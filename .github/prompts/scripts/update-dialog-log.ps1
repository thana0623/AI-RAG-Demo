param(
    [string]$Date = (Get-Date -Format 'yyyy-MM-dd'),
    [Parameter(Mandatory = $true)][string]$Title,
    [Parameter(Mandatory = $true)][string]$Request,
    [string[]]$Changes = @(),
    [string[]]$Decisions = @(),
    [string[]]$Todos = @(),
    [string]$StateDelta = '',
    [string]$CarryForward = '',
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$promptsDir = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$dailyDir = Join-Path $promptsDir 'daily'
$recentPath = Join-Path $promptsDir 'recent-5.md'
$summaryPath = Join-Path $promptsDir 'summary-10.md'
$statePath = Join-Path $promptsDir 'log-state.json'

function Clean-Text {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) { return '' }

    $clean = $Text
    $fillerRegex = '(?i)^(great|sure|ok|okay|got it|happy to help|i can help|absolutely)[\s,.:;!-]*'
    $clean = [regex]::Replace($clean, $fillerRegex, '')
    $clean = [regex]::Replace($clean, '\s+', ' ')
    $clean = $clean.Trim(' ', ',', '.', '!', '?', ';', ':')
    return $clean
}

function New-Bullets {
    param([string[]]$Items)
    if (-not $Items -or @($Items).Count -eq 0) { return @('  - (none)') }
    return $Items | ForEach-Object { "  - $_" }
}

function Get-EntryBlocks {
    param([string]$Content)
    $entryRegex = '(?ms)^## Entry-\d{3}\r?\n.*?(?=^## Entry-\d{3}\r?\n|\z)'
    return [regex]::Matches($Content, $entryRegex)
}

function Parse-RecentIds {
    param([string]$Content)
    $ids = @()
    $m = [regex]::Matches($Content, '(?m)^## Entry-(\d{3})\r?$')
    foreach ($x in $m) { $ids += [int]$x.Groups[1].Value }
    return $ids
}

function Load-State {
    if (Test-Path $statePath) {
        return Get-Content $statePath -Raw -Encoding UTF8 | ConvertFrom-Json
    }

    $maxId = 0
    if (Test-Path $recentPath) {
        $recent = Get-Content $recentPath -Raw -Encoding UTF8
        $ids = @(Parse-RecentIds -Content $recent)
        if (@($ids).Count -gt 0) {
            $maxId = ($ids | Measure-Object -Maximum).Maximum
        }
    }

    $windowIdNum = 1
    $windowCount = 0
    if (Test-Path $summaryPath) {
        $summary = Get-Content $summaryPath -Raw -Encoding UTF8
        if ($summary -match '(?m)^- window_id: W-(\d{4})$') {
            $windowIdNum = [int]([string]$Matches[1])
        }
        if ($summary -match '(?m)^- 当前已收录: (\d+) / 10$') {
            $windowCount = [int]([string]$Matches[1])
        }
    }

    $windowStart = if ($maxId -gt 0 -and $windowCount -gt 0) { $maxId - $windowCount + 1 } elseif ($maxId -gt 0) { $maxId + 1 } else { 1 }

    return [pscustomobject]@{
        nextEntryId = $maxId + 1
        windowId = ('W-{0:D4}' -f $windowIdNum)
        windowStartEntry = $windowStart
        windowCount = $windowCount
        windowEntries = @()
    }
}

function Save-State {
    param($State)
    $json = $State | ConvertTo-Json -Depth 6
    if ($DryRun) {
        Write-Host '[DryRun] Skip write state file'
    } else {
        Set-Content -Path $statePath -Value $json -Encoding UTF8
    }
}

function Ensure-RecentFile {
    if (Test-Path $recentPath) { return }
    $base = @(
        '# Recent 5 Dialog + Action Window',
        '',
        '> Keep only latest 5 entries. One dialog + action set = one entry.',
        ''
    ) -join "`r`n"
    if ($DryRun) { Write-Host '[DryRun] Would create recent-5.md' }
    else { Set-Content -Path $recentPath -Value $base -Encoding UTF8 }
}

function Update-Daily {
    param(
        [int]$EntryId,
        [string]$CleanRequest,
        [string[]]$ChangeLines,
        [string[]]$DecisionLines,
        [string[]]$TodoLines
    )

    if (-not (Test-Path $dailyDir)) {
        if (-not $DryRun) { New-Item -ItemType Directory -Path $dailyDir | Out-Null }
    }

    $dailyPath = Join-Path $dailyDir ("$Date.md")
    if (-not (Test-Path $dailyPath)) {
        $header = "# $Date Dialog Log`r`n"
        if ($DryRun) { Write-Host "[DryRun] Would create $dailyPath" }
        else { Set-Content -Path $dailyPath -Value $header -Encoding UTF8 }
    }

    $existing = if (Test-Path $dailyPath) { Get-Content $dailyPath -Raw -Encoding UTF8 } else { "# $Date Dialog Log`r`n" }
    $sessionNo = ([regex]::Matches($existing, '(?m)^## Session \d+:')).Count + 1

    $block = @()
    $block += "## Session ${sessionNo}: $Title"
    $block += "- Request: $CleanRequest"
    $block += ('- Linked Entry: Entry-{0:D3}' -f $EntryId)
    $block += '- Code Changes:'
    $block += (New-Bullets -Items $ChangeLines)
    $block += '- Technical Decisions:'
    $block += (New-Bullets -Items $DecisionLines)
    $block += '- TODO:'
    $block += (New-Bullets -Items $TodoLines)

    $append = "`r`n" + ($block -join "`r`n") + "`r`n"
    if ($DryRun) { Write-Host "[DryRun] Would append to $dailyPath" }
    else { Add-Content -Path $dailyPath -Value $append -Encoding UTF8 }

    return $dailyPath
}

function Update-Recent {
    param(
        [int]$EntryId,
        [string]$CleanRequest,
        [string[]]$ChangeLines,
        [string[]]$DecisionLines,
        [string[]]$TodoLines
    )

    Ensure-RecentFile
    $raw = Get-Content $recentPath -Raw -Encoding UTF8
    $matches = Get-EntryBlocks -Content $raw

    $header = $raw.TrimEnd()
    $blocks = @()
    if ($matches.Count -gt 0) {
        $header = $raw.Substring(0, $matches[0].Index).TrimEnd()
        foreach ($m in $matches) { $blocks += $m.Value.TrimEnd() }
    }

    $entry = @()
    $entry += ('## Entry-{0:D3}' -f $EntryId)
    $entry += "- Date: $Date"
    $entry += "- Clean Request: $CleanRequest"
    $entry += '- Code Changes:'
    $entry += (New-Bullets -Items $ChangeLines)
    $entry += '- Technical Decisions:'
    $entry += (New-Bullets -Items $DecisionLines)
    $entry += '- TODO:'
    $entry += (New-Bullets -Items $TodoLines)

    $blocks += ($entry -join "`r`n")
    if ($blocks.Count -gt 5) {
        $blocks = $blocks[($blocks.Count - 5)..($blocks.Count - 1)]
    }

    $newText = $header + "`r`n`r`n" + ($blocks -join "`r`n`r`n") + "`r`n"
    if ($DryRun) { Write-Host "[DryRun] Would update $recentPath" }
    else { Set-Content -Path $recentPath -Value $newText -Encoding UTF8 }
}

function Update-Summary {
    param($State, [int]$CurrentEntryId)

    $startEntry = [int]([string](@($State.windowStartEntry)[0]))
    $endEntry = $startEntry + 9

    $decisions = @($State.windowEntries | ForEach-Object { $_.decisions } | Where-Object { $_ } | Select-Object -Unique)
    $todos = @($State.windowEntries | ForEach-Object { $_.todos } | Where-Object { $_ } | Select-Object -Unique)

    $currentState = @()
    if ($StateDelta) { $currentState += "- $StateDelta" }
    else {
        $currentState += ('- Window progress: {0}/10, latest Entry-{1:D3}.' -f $State.windowCount, $CurrentEntryId)
        $currentState += '- Daily and recent-5 files were updated by script.'
    }

    $keepLines = if ($decisions.Count -gt 0) { $decisions | ForEach-Object { "- $_" } } else { @('- (none)') }
    $todoLines = if ($todos.Count -gt 0) { $todos | ForEach-Object { "- $_" } } else { @('- (none)') }

    $carry = @()
    if ($CarryForward) { $carry += "- $CarryForward" }
    elseif ($todos.Count -gt 0) { $carry += "- Prioritize: $($todos[0])" }
    else { $carry += '- Continue appending next entry.' }

    $lines = @()
    $lines += '# 近 10 条对话状态摘要（Stateful）'
    $lines += ''
    $lines += '> 用途：每累计 10 条对话与操作后，输出一段有状态摘要，沉淀可延续上下文。'
    $lines += ''
    $lines += '## 窗口元数据'
    $lines += "- window_id: $($State.windowId)"
    $lines += ('- 统计范围: Entry-{0:D3} ~ Entry-{1:D3}' -f $startEntry, $endEntry)
    $lines += ('- 当前已收录: {0} / 10' -f $State.windowCount)
    $lines += '- 数据来源:'
    $lines += '  - .github/prompts/recent-5.md'
    $lines += ('  - .github/prompts/daily/{0}.md' -f $Date)
    $lines += ''
    $lines += if ($State.windowCount -ge 10) { '## Stateful 摘要（自动生成）' } else { '## Stateful 摘要（草稿）' }
    $lines += '### Current State'
    $lines += $currentState
    $lines += ''
    $lines += '### Decisions Kept'
    $lines += $keepLines
    $lines += ''
    $lines += '### Open TODO'
    $lines += $todoLines
    $lines += ''
    $lines += '### Carry Forward'
    $lines += $carry
    $lines += ''
    $lines += '## 10 条压缩模板（用于满 10 条时替换）'
    $lines += '- Window: W-XXXX（Entry-AAA ~ Entry-BBB）'
    $lines += '- Delta State: 本窗口相对上窗口的核心变化'
    $lines += '- Stable Decisions: 仍有效且继续沿用的决策'
    $lines += '- Invalidated Decisions: 已废弃或替换的决策'
    $lines += '- Key File Changes: 本窗口关键文件变更集合'
    $lines += '- Pending TODO: 延续到下一窗口的待办'
    $lines += '- Next Actions: 下一个窗口第一优先级动作'

    $text = ($lines -join "`r`n") + "`r`n"
    if ($DryRun) { Write-Host "[DryRun] Would update $summaryPath" }
    else { Set-Content -Path $summaryPath -Value $text -Encoding UTF8 }

    if ($State.windowCount -ge 10) {
        $windowNum = [int]([string]((@($State.windowId)[0]) -replace 'W-', ''))
        $State.windowId = ('W-{0:D4}' -f ($windowNum + 1))
        $State.windowStartEntry = $CurrentEntryId + 1
        $State.windowCount = 0
        $State.windowEntries = @()
    }
}

$cleanRequest = Clean-Text -Text $Request
if (-not $cleanRequest) { throw 'Request became empty after cleaning.' }

$cleanChanges = @($Changes | Where-Object { $_ -and $_.Trim() -ne '' } | ForEach-Object { $_.Trim() })
$cleanDecisions = @($Decisions | Where-Object { $_ -and $_.Trim() -ne '' } | ForEach-Object { $_.Trim() })
$cleanTodos = @($Todos | Where-Object { $_ -and $_.Trim() -ne '' } | ForEach-Object { $_.Trim() })

$state = @(Load-State)[0]
$entryId = [int]([string](@($state.nextEntryId)[0]))

Update-Daily -EntryId $entryId -CleanRequest $cleanRequest -ChangeLines $cleanChanges -DecisionLines $cleanDecisions -TodoLines $cleanTodos | Out-Null
Update-Recent -EntryId $entryId -CleanRequest $cleanRequest -ChangeLines $cleanChanges -DecisionLines $cleanDecisions -TodoLines $cleanTodos

$newEntry = [pscustomobject]@{
    id = $entryId
    date = $Date
    request = $cleanRequest
    changes = $cleanChanges
    decisions = $cleanDecisions
    todos = $cleanTodos
}

$state.windowEntries = @($state.windowEntries) + $newEntry
if (@($state.windowEntries).Count -gt 10) {
    $entryCount = @($state.windowEntries).Count
    $state.windowEntries = @($state.windowEntries)[($entryCount - 10)..($entryCount - 1)]
}

$state.windowCount = [int]([string](@($state.windowCount)[0])) + 1
Update-Summary -State $state -CurrentEntryId $entryId

$state.nextEntryId = $entryId + 1
Save-State -State $state

if ($DryRun) {
    Write-Host ('[DryRun] Entry-{0:D3} simulation complete.' -f $entryId)
} else {
    Write-Host ('Entry-{0:D3} recorded. recent-5 trimmed and summary-10 updated.' -f $entryId)
}
