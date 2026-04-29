export function clampText(value: string, maxLength = 120): string {
  if (value.length <= maxLength) return value
  return `${value.slice(0, maxLength)}...`
}
