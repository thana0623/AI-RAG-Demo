import { storeToRefs } from 'pinia'
import { useUserStore } from '@/store/modules/user'

export function useUser() {
  const store = useUserStore()
  const { token, profile, isLoggedIn, username, email } = storeToRefs(store)

  return {
    store,
    token,
    profile,
    isLoggedIn,
    username,
    email
  }
}
