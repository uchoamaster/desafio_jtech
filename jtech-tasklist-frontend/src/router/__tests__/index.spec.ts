import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { installTestStorage } from '@/stores/__tests__/testStorage'

describe('router guards', () => {
  beforeEach(async () => {
    installTestStorage().clear()
    setActivePinia(createPinia())
    await router.replace('/login')
  })

  it('redirects unauthenticated users away from protected routes', async () => {
    await router.push('/app')

    expect(router.currentRoute.value.name).toBe('login')
  })

  it('redirects authenticated users away from guest-only routes', async () => {
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }

    await router.replace('/app')
    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('keeps access to protected routes after restoring the session from storage', async () => {
    window.localStorage.setItem(
      'jtech-tasklist.auth',
      JSON.stringify({
        username: 'angelo',
        email: 'angelo@tasklist.local',
        token: 'jwt-token',
      }),
    )

    setActivePinia(createPinia())

    await router.push('/app')

    expect(router.currentRoute.value.name).toBe('dashboard')
  })
})