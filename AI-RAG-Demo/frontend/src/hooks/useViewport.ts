import { ref, onMounted, onBeforeUnmount } from 'vue'

export function useViewport() {
  const width = ref(window.innerWidth)

  const onResize = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    window.addEventListener('resize', onResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
  })

  const isMobile = () => width.value < 768

  return {
    width,
    isMobile
  }
}
