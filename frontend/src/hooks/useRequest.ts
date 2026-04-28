import { ref } from 'vue'

export function useRequest<T>(handler: () => Promise<T>) {
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const run = async () => {
    loading.value = true
    error.value = null
    try {
      return await handler()
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    run
  }
}
