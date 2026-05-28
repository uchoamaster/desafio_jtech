import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useAuthStore } from '@/stores/auth'
import { installTestStorage } from '@/stores/__tests__/testStorage'
import { useTaskBoardStore } from '@/stores/taskBoard'

describe('task board store', () => {
  beforeEach(() => {
    installTestStorage().clear()
    setActivePinia(createPinia())
    vi.stubGlobal('fetch', vi.fn())

    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }
  })

  it('loads lists from the API', async () => {
    const taskBoardStore = useTaskBoardStore()
    const fetchMock = vi.mocked(fetch)

    fetchMock.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ([
        { id: 'list-1', name: 'Trabalho', tasks: [] },
        { id: 'list-2', name: 'Estudos', tasks: [] },
      ]),
    } as Response)

    await taskBoardStore.fetchLists()

    expect(taskBoardStore.lists.map((list) => list.name)).toEqual(['Trabalho', 'Estudos'])
    expect(taskBoardStore.currentList?.name).toBe('Trabalho')
  })

  it('creates and updates lists through the API', async () => {
    const taskBoardStore = useTaskBoardStore()
    const fetchMock = vi.mocked(fetch)

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({ id: 'list-1', name: 'Trabalho', tasks: [] }),
    } as Response)

    await expect(taskBoardStore.createList('Trabalho')).resolves.toEqual({ ok: true })

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({ id: 'list-1', name: 'Trabalho 2', tasks: [] }),
    } as Response)

    await expect(taskBoardStore.renameList('list-1', 'Trabalho 2')).resolves.toEqual({ ok: true })
    expect(taskBoardStore.currentList?.name).toBe('Trabalho 2')
  })

  it('updates and toggles an existing task through the API', async () => {
    const taskBoardStore = useTaskBoardStore()
    const fetchMock = vi.mocked(fetch)

    taskBoardStore.lists = [
      {
        id: 'list-1',
        name: 'Trabalho',
        tasks: [{ id: 'task-1', title: 'Revisar PR', notes: 'Validar criterios', completed: false }],
      },
    ]
    taskBoardStore.selectList('list-1')

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        id: 'list-1',
        name: 'Trabalho',
        tasks: [{ id: 'task-1', title: 'Revisar PR', notes: 'Validar criterios', completed: true }],
      }),
    } as Response)

    await expect(taskBoardStore.toggleTask('list-1', 'task-1', true)).resolves.toEqual({ ok: true })
    expect(taskBoardStore.currentList?.tasks[0]?.completed).toBe(true)

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        id: 'list-1',
        name: 'Trabalho',
        tasks: [{ id: 'task-1', title: 'Revisar PR final', notes: 'Swagger + H2', completed: true }],
      }),
    } as Response)

    await expect(taskBoardStore.updateTask('list-1', 'task-1', 'Revisar PR final', 'Swagger + H2')).resolves.toEqual({
      ok: true,
    })
    expect(taskBoardStore.currentList?.tasks[0]?.title).toBe('Revisar PR final')
  })

  it('restores the persisted board state for the authenticated user', () => {
    const storage = installTestStorage()
    storage.setItem(
      'jtech-tasklist.board.angelo@tasklist.local',
      JSON.stringify({
        lists: [
          {
            id: 'list-1',
            name: 'Trabalho',
            tasks: [{ id: 'task-1', title: 'Revisar PR', notes: 'Persistida', completed: false }],
          },
        ],
        selectedListId: 'list-1',
      }),
    )

    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }

    const taskBoardStore = useTaskBoardStore()

    expect(taskBoardStore.currentList?.name).toBe('Trabalho')
    expect(taskBoardStore.currentList?.tasks[0]?.title).toBe('Revisar PR')
  })

  it('supports local CRUD when using a simulated session', async () => {
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'mock-session:angelo@tasklist.local',
    }

    const taskBoardStore = useTaskBoardStore()

    await expect(taskBoardStore.createList('Trabalho')).resolves.toEqual({ ok: true })
    const listId = taskBoardStore.currentList?.id

    expect(listId).toBeTruthy()

    await expect(taskBoardStore.createTask(listId!, 'Revisar PR', 'Modo local')).resolves.toEqual({ ok: true })
    expect(taskBoardStore.currentList?.tasks[0]?.title).toBe('Revisar PR')

    const taskId = taskBoardStore.currentList?.tasks[0]?.id
    await expect(taskBoardStore.toggleTask(listId!, taskId!, true)).resolves.toEqual({ ok: true })
    expect(taskBoardStore.currentList?.tasks[0]?.completed).toBe(true)

    await expect(taskBoardStore.updateTask(listId!, taskId!, 'Revisar PR final', 'Persistido local')).resolves.toEqual({ ok: true })
    expect(taskBoardStore.currentList?.tasks[0]?.title).toBe('Revisar PR final')
  })
})