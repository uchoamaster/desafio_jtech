import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useAuthStore } from '@/stores/auth'
import { installTestStorage } from '@/stores/__tests__/testStorage'

describe('auth store', () => {
  beforeEach(() => {
    installTestStorage().clear()
    setActivePinia(createPinia())
    vi.stubGlobal('fetch', vi.fn())
  })

  it('blocks empty credentials', async () => {
    const authStore = useAuthStore()

    await expect(authStore.login('', '')).resolves.toEqual({
      ok: false,
      error: 'Informe usuario e senha para continuar.',
    })
    expect(authStore.isAuthenticated).toBe(false)
  })

  it('persists the authenticated user after API login', async () => {
    const authStore = useAuthStore()
    const fetchMock = vi.mocked(fetch)

    fetchMock.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        token: 'jwt-token',
        displayName: 'angelo',
        email: 'angelo@tasklist.local',
      }),
    } as Response)

    await expect(authStore.login('angelo', '123456')).resolves.toEqual({ ok: true })

    setActivePinia(createPinia())
    const restoredStore = useAuthStore()

    expect(restoredStore.user?.username).toBe('angelo')
    expect(restoredStore.user?.token).toBe('jwt-token')
    expect(restoredStore.isAuthenticated).toBe(true)
  })

  it('clears the persisted session on logout', async () => {
    const authStore = useAuthStore()
    const fetchMock = vi.mocked(fetch)

    fetchMock.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        token: 'jwt-token',
        displayName: 'angelo',
        email: 'angelo@tasklist.local',
      }),
    } as Response)

    await authStore.login('angelo', '123456')
    expect(window.localStorage.getItem('jtech-tasklist.auth')).not.toBeNull()

    authStore.logout()

    expect(window.localStorage.getItem('jtech-tasklist.auth')).toBeNull()
    expect(authStore.isAuthenticated).toBe(false)
  })

  it('falls back to a simulated session when the API is unavailable', async () => {
    const authStore = useAuthStore()
    const fetchMock = vi.mocked(fetch)

    fetchMock.mockRejectedValue(new Error('Network unavailable'))

    await expect(authStore.login('angelo', '123456')).resolves.toEqual({ ok: true })

    expect(authStore.user?.username).toBe('angelo')
    expect(authStore.user?.token.startsWith('mock-session:')).toBe(true)
    expect(authStore.isAuthenticated).toBe(true)
  })
})