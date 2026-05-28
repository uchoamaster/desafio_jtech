import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { ApiError, apiRequest, createLocalEmail, type AuthApiResponse } from '@/services/api'

type LoginResult =
  | { ok: true }
  | { ok: false; error: string }

export interface AuthUser {
  username: string
  email: string
  token: string
}

const STORAGE_KEY = 'jtech-tasklist.auth'
export const MOCK_TOKEN_PREFIX = 'mock-session:'

export function isMockToken(token: string | null | undefined) {
  return typeof token === 'string' && token.startsWith(MOCK_TOKEN_PREFIX)
}

function readStoredUser(): AuthUser | null {
  if (typeof window === 'undefined') {
    return null
  }

  const rawValue = window.localStorage.getItem(STORAGE_KEY)

  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue) as AuthUser
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(readStoredUser())
  const token = computed(() => user.value?.token ?? null)
  const isAuthenticated = computed(() => token.value !== null)
  const displayName = computed(() => user.value?.username ?? 'Visitante')

  function persist() {
    if (typeof window === 'undefined') {
      return
    }

    if (user.value) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(user.value))
      return
    }

    window.localStorage.removeItem(STORAGE_KEY)
  }

  async function login(username: string, password: string): Promise<LoginResult> {
    const normalizedUsername = username.trim()
    const normalizedPassword = password.trim()

    if (!normalizedUsername || !normalizedPassword) {
      return { ok: false, error: 'Informe usuario e senha para continuar.' }
    }

    const loginPayload = {
      email: createLocalEmail(normalizedUsername),
      password: normalizedPassword,
    }

    try {
      let response: AuthApiResponse

      try {
        response = await apiRequest<AuthApiResponse>('/auth/login', {
          method: 'POST',
          body: loginPayload,
        })
      } catch (error) {
        if (!(error instanceof ApiError) || error.status !== 401) {
          throw error
        }

        await apiRequest<AuthApiResponse>('/auth/register', {
          method: 'POST',
          body: {
            name: normalizedUsername,
            email: loginPayload.email,
            password: normalizedPassword,
          },
        })

        response = await apiRequest<AuthApiResponse>('/auth/login', {
          method: 'POST',
          body: loginPayload,
        })
      }

      user.value = {
        username: response.displayName || normalizedUsername,
        email: response.email,
        token: response.token,
      }

      persist()
      return { ok: true }
    } catch (error) {
      if (error instanceof ApiError) {
        return { ok: false, error: error.message }
      }

      user.value = {
        username: normalizedUsername,
        email: loginPayload.email,
        token: `${MOCK_TOKEN_PREFIX}${loginPayload.email}`,
      }

      persist()
      return { ok: true }
    }
  }

  function logout() {
    user.value = null
    persist()
  }

  return {
    displayName,
    isAuthenticated,
    login,
    logout,
    token,
    user,
  }
})