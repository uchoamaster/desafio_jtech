import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'

import { ApiError, apiRequest, type ApiTaskList } from '@/services/api'
import { isMockToken, useAuthStore } from '@/stores/auth'

type MutationResult =
  | { ok: true }
  | { ok: false; error: string }

export interface TaskItem {
  id: string
  title: string
  notes: string
  completed: boolean
}

export interface TaskList {
  id: string
  name: string
  tasks: TaskItem[]
}

interface PersistedBoardState {
  lists: TaskList[]
  selectedListId: string | null
}

const STORAGE_PREFIX = 'jtech-tasklist.board'

function createId(prefix: string) {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return `${prefix}-${crypto.randomUUID()}`
  }

  return `${prefix}-${Math.random().toString(36).slice(2, 10)}`
}

function getStorageKey(email: string) {
  return `${STORAGE_PREFIX}.${email.toLowerCase()}`
}

function readStoredBoard(email: string | null | undefined): PersistedBoardState | null {
  if (typeof window === 'undefined' || !email) {
    return null
  }

  const rawValue = window.localStorage.getItem(getStorageKey(email))

  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue) as PersistedBoardState
  } catch {
    window.localStorage.removeItem(getStorageKey(email))
    return null
  }
}

export const useTaskBoardStore = defineStore('task-board', () => {
  const authStore = useAuthStore()
  const initialSnapshot = readStoredBoard(authStore.user?.email)
  const lists = ref<TaskList[]>(initialSnapshot?.lists ?? [])
  const selectedListId = ref<string | null>(initialSnapshot?.selectedListId ?? null)
  const loading = ref(false)
  const syncing = ref(false)
  const ready = ref(false)
  const lastError = ref('')

  const currentList = computed(() => {
    if (!selectedListId.value) {
      return lists.value[0] ?? null
    }

    return lists.value.find((list) => list.id === selectedListId.value) ?? lists.value[0] ?? null
  })

  const hasLists = computed(() => lists.value.length > 0)
  const totalTasks = computed(() => lists.value.reduce((count, list) => count + list.tasks.length, 0))
  const completedTasks = computed(() => lists.value.reduce((count, list) => count + list.tasks.filter((task) => task.completed).length, 0))
  const usingMockSession = computed(() => isMockToken(authStore.token))

  function persistBoard() {
    const email = authStore.user?.email

    if (typeof window === 'undefined' || !email) {
      return
    }

    if (!lists.value.length) {
      window.localStorage.removeItem(getStorageKey(email))
      return
    }

    window.localStorage.setItem(
      getStorageKey(email),
      JSON.stringify({
        lists: lists.value,
        selectedListId: selectedListId.value,
      } satisfies PersistedBoardState),
    )
  }

  function restoreBoard() {
    const snapshot = readStoredBoard(authStore.user?.email)

    if (!snapshot) {
      lists.value = []
      selectedListId.value = null
      return
    }

    lists.value = snapshot.lists
    selectedListId.value = snapshot.selectedListId
  }

  function normalizeList(list: ApiTaskList): TaskList {
    return {
      id: list.id,
      name: list.name,
      tasks: list.tasks.map((task) => ({
        id: task.id,
        title: task.title,
        notes: task.notes,
        completed: task.completed,
      })),
    }
  }

  function applyLists(nextLists: ApiTaskList[]) {
    lists.value = nextLists.map(normalizeList)

    if (!lists.value.length) {
      selectedListId.value = null
      persistBoard()
      return
    }

    if (!selectedListId.value || !lists.value.some((list) => list.id === selectedListId.value)) {
      selectedListId.value = lists.value[0].id
    }

    persistBoard()
  }

  function replaceList(nextList: ApiTaskList) {
    const normalized = normalizeList(nextList)
    const currentIndex = lists.value.findIndex((list) => list.id === normalized.id)

    if (currentIndex === -1) {
      lists.value = [...lists.value, normalized]
    } else {
      const nextItems = [...lists.value]
      nextItems.splice(currentIndex, 1, normalized)
      lists.value = nextItems
    }

    selectedListId.value = normalized.id
    persistBoard()
  }

  function clearBoard() {
    const email = authStore.user?.email

    if (typeof window !== 'undefined' && email) {
      window.localStorage.removeItem(getStorageKey(email))
    }

    lists.value = []
    selectedListId.value = null
    lastError.value = ''
    ready.value = false
  }

  function ensureToken() {
    if (!authStore.token) {
      throw new ApiError('Faça login para continuar.', 401)
    }
  }

  function handleMutationError(error: unknown, fallbackMessage: string): MutationResult {
    if (error instanceof ApiError && error.status === 401) {
      authStore.logout()
    }

    const message = error instanceof Error ? error.message : fallbackMessage
    lastError.value = message
    return { ok: false, error: message }
  }

  async function fetchLists() {
    if (!authStore.token) {
      clearBoard()
      return
    }

    if (usingMockSession.value) {
      restoreBoard()
      loading.value = false
      lastError.value = ''
      ready.value = true
      return
    }

    loading.value = true
    lastError.value = ''

    try {
      const response = await apiRequest<ApiTaskList[]>('/api/v1/tasklists', {
        token: authStore.token,
      })
      applyLists(response)
    } catch (error) {
      handleMutationError(error, 'Nao foi possivel carregar as listas.')
    } finally {
      loading.value = false
      ready.value = true
    }
  }

  async function createStarterLists() {
    for (const name of ['Trabalho', 'Estudos', 'Pessoal']) {
      const result = await createList(name)

      if (!result.ok) {
        return result
      }
    }

    return { ok: true } as MutationResult
  }

  async function createList(name: string): Promise<MutationResult> {
    const normalizedName = name.trim()

    if (!normalizedName) {
      return { ok: false, error: 'Informe um nome para a lista.' }
    }

    try {
      ensureToken()
      if (usingMockSession.value) {
        const duplicate = lists.value.some((list) => list.name.toLowerCase() === normalizedName.toLowerCase())

        if (duplicate) {
          return { ok: false, error: 'Ja existe uma lista com esse nome.' }
        }

        lists.value = [...lists.value, { id: createId('list'), name: normalizedName, tasks: [] }]
        selectedListId.value = lists.value.at(-1)?.id ?? null
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>('/api/v1/tasklists', {
        method: 'POST',
        token: authStore.token,
        body: { name: normalizedName },
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel criar a lista.')
    } finally {
      syncing.value = false
    }
  }

  async function renameList(listId: string, name: string): Promise<MutationResult> {
    const normalizedName = name.trim()

    if (!normalizedName) {
      return { ok: false, error: 'Informe um nome valido para a lista.' }
    }

    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)

        if (!target) {
          return { ok: false, error: 'Lista nao encontrada.' }
        }

        const duplicate = lists.value.some(
          (list) => list.id !== listId && list.name.toLowerCase() === normalizedName.toLowerCase(),
        )

        if (duplicate) {
          return { ok: false, error: 'Ja existe uma lista com esse nome.' }
        }

        target.name = normalizedName
        lists.value = [...lists.value]
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>(`/api/v1/tasklists/${listId}`, {
        method: 'PUT',
        token: authStore.token,
        body: { name: normalizedName },
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel renomear a lista.')
    } finally {
      syncing.value = false
    }
  }

  async function removeList(listId: string): Promise<MutationResult> {
    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)

        if (!target) {
          return { ok: false, error: 'Lista nao encontrada.' }
        }

        if (target.tasks.length) {
          return { ok: false, error: 'Remova as tarefas antes de excluir a lista.' }
        }

        lists.value = lists.value.filter((item) => item.id !== listId)
        if (selectedListId.value === listId) {
          selectedListId.value = lists.value[0]?.id ?? null
        }
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      await apiRequest<void>(`/api/v1/tasklists/${listId}`, {
        method: 'DELETE',
        token: authStore.token,
      })
      lists.value = lists.value.filter((item) => item.id !== listId)
      if (selectedListId.value === listId) {
        selectedListId.value = lists.value[0]?.id ?? null
      }
      lastError.value = ''
      persistBoard()
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel remover a lista.')
    } finally {
      syncing.value = false
    }
  }

  function selectList(listId: string) {
    if (!lists.value.some((list) => list.id === listId)) {
      return null
    }

    selectedListId.value = listId
    persistBoard()
  }

  async function createTask(listId: string, title: string, notes: string): Promise<MutationResult> {
    const normalizedTitle = title.trim()
    const normalizedNotes = notes.trim()

    if (!normalizedTitle) {
      return { ok: false, error: 'Informe um titulo para a tarefa.' }
    }

    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)

        if (!target) {
          return { ok: false, error: 'Lista nao encontrada.' }
        }

        const duplicate = target.tasks.some((task) => task.title.toLowerCase() === normalizedTitle.toLowerCase())

        if (duplicate) {
          return { ok: false, error: 'Ja existe uma tarefa com esse titulo na lista.' }
        }

        target.tasks = [{ id: createId('task'), title: normalizedTitle, notes: normalizedNotes, completed: false }, ...target.tasks]
        lists.value = [...lists.value]
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>(`/api/v1/tasklists/${listId}/tasks`, {
        method: 'POST',
        token: authStore.token,
        body: {
          title: normalizedTitle,
          notes: normalizedNotes,
        },
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel criar a tarefa.')
    } finally {
      syncing.value = false
    }
  }

  async function updateTask(listId: string, taskId: string, title: string, notes: string): Promise<MutationResult> {
    const normalizedTitle = title.trim()
    const normalizedNotes = notes.trim()

    if (!normalizedTitle) {
      return { ok: false, error: 'Informe um titulo valido para a tarefa.' }
    }

    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)
        const task = target?.tasks.find((item) => item.id === taskId)

        if (!target || !task) {
          return { ok: false, error: 'Tarefa nao encontrada.' }
        }

        const duplicate = target.tasks.some(
          (item) => item.id !== taskId && item.title.toLowerCase() === normalizedTitle.toLowerCase(),
        )

        if (duplicate) {
          return { ok: false, error: 'Ja existe uma tarefa com esse titulo na lista.' }
        }

        task.title = normalizedTitle
        task.notes = normalizedNotes
        lists.value = [...lists.value]
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>(`/api/v1/tasklists/${listId}/tasks/${taskId}`, {
        method: 'PUT',
        token: authStore.token,
        body: {
          title: normalizedTitle,
          notes: normalizedNotes,
        },
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel atualizar a tarefa.')
    } finally {
      syncing.value = false
    }
  }

  async function toggleTask(listId: string, taskId: string, completed: boolean): Promise<MutationResult> {
    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)
        const task = target?.tasks.find((item) => item.id === taskId)

        if (!target || !task) {
          return { ok: false, error: 'Tarefa nao encontrada.' }
        }

        task.completed = completed
        lists.value = [...lists.value]
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>(`/api/v1/tasklists/${listId}/tasks/${taskId}/status`, {
        method: 'PATCH',
        token: authStore.token,
        body: { completed },
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel atualizar o status da tarefa.')
    } finally {
      syncing.value = false
    }
  }

  async function deleteTask(listId: string, taskId: string): Promise<MutationResult> {
    try {
      ensureToken()
      if (usingMockSession.value) {
        const target = lists.value.find((list) => list.id === listId)

        if (!target) {
          return { ok: false, error: 'Lista nao encontrada.' }
        }

        target.tasks = target.tasks.filter((item) => item.id !== taskId)
        lists.value = [...lists.value]
        lastError.value = ''
        persistBoard()
        return { ok: true }
      }

      syncing.value = true
      const response = await apiRequest<ApiTaskList>(`/api/v1/tasklists/${listId}/tasks/${taskId}`, {
        method: 'DELETE',
        token: authStore.token,
      })
      replaceList(response)
      lastError.value = ''
      return { ok: true }
    } catch (error) {
      return handleMutationError(error, 'Nao foi possivel remover a tarefa.')
    } finally {
      syncing.value = false
    }
  }

  watch(
    () => [authStore.token, authStore.user?.email],
    async () => {
      restoreBoard()
      await fetchLists()
    },
    { immediate: true },
  )

  return {
    createStarterLists,
    completedTasks,
    createList,
    createTask,
    currentList,
    deleteTask,
    fetchLists,
    hasLists,
    lastError,
    lists,
    loading,
    ready,
    removeList,
    renameList,
    selectList,
    syncing,
    toggleTask,
    totalTasks,
    updateTask,
  }
})