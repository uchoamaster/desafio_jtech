export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  token?: string | null
}

export interface AuthApiResponse {
  token: string
  displayName: string
  email: string
}

export interface ApiTaskItem {
  id: string
  title: string
  notes: string
  completed: boolean
}

export interface ApiTaskList {
  id: string
  name: string
  tasks: ApiTaskItem[]
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080'

export function createLocalEmail(username: string) {
  const normalized = username
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '.')
    .replace(/[^a-z0-9._-]/g, '')

  return normalized.includes('@') ? normalized : `${normalized}@tasklist.local`
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (!response.ok) {
    let message = 'Nao foi possivel concluir a requisicao.'

    try {
      const data = (await response.json()) as { message?: string; debugMessage?: string }
      message = data.message ?? data.debugMessage ?? message
    } catch {
      message = response.statusText || message
    }

    throw new ApiError(message, response.status)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}