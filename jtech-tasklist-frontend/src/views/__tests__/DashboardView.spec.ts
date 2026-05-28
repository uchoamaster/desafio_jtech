import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

import DashboardView from '@/views/DashboardView.vue'
import { useAuthStore } from '@/stores/auth'
import { useTaskBoardStore } from '@/stores/taskBoard'
import { installTestStorage } from '@/stores/__tests__/testStorage'
import { componentStubs, installMatchMediaMock } from '@/test-utils/componentStubs'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

describe('DashboardView', () => {
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    installTestStorage().clear()
    installMatchMediaMock()
    pinia = createPinia()
    setActivePinia(pinia)
    pushMock.mockReset()
    vi.stubGlobal('fetch', vi.fn())
    vi.stubGlobal('confirm', vi.fn(() => true))
  })

  it('renders the active list and tasks loaded from the store', async () => {
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ([
        {
          id: 'list-1',
          name: 'Trabalho',
          tasks: [{ id: 'task-1', title: 'Revisar PR', notes: 'Cobrir criterios', completed: false }],
        },
      ]),
    } as Response)

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [pinia],
        stubs: componentStubs,
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Trabalho')
    expect(wrapper.text()).toContain('Revisar PR')
    expect(wrapper.text()).toContain('angelo')
  })

  it('creates a new list from the dashboard form', async () => {
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }

    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ([]),
    } as Response)
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({ id: 'list-1', name: 'Trabalho', tasks: [] }),
    } as Response)

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [pinia],
        stubs: componentStubs,
      },
    })

    await flushPromises()

    const listInput = wrapper.get('input[aria-label="Nova lista"]')
    await listInput.setValue('Trabalho')
    const createListButton = wrapper.findAll('button').find((candidate) => candidate.text() === 'Criar lista')
    expect(createListButton).toBeDefined()
    await createListButton!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Lista criada com sucesso.')
    expect(wrapper.text()).toContain('Trabalho')
  })

  it('logs out the user and redirects to login', async () => {
    const authStore = useAuthStore()
    authStore.user = {
      username: 'angelo',
      email: 'angelo@tasklist.local',
      token: 'jwt-token',
    }

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ([]),
    } as Response)

    const wrapper = mount(DashboardView, {
      global: {
        plugins: [pinia],
        stubs: componentStubs,
      },
    })

    await flushPromises()

    const logoutButton = wrapper.findAll('button').find((candidate) => candidate.text() === 'Sair')
    expect(logoutButton).toBeDefined()
    await logoutButton!.trigger('click')

    const taskBoardStore = useTaskBoardStore()
    expect(authStore.user).toBeNull()
    expect(taskBoardStore.lists).toEqual([])
    expect(pushMock).toHaveBeenCalledWith({ name: 'login' })
  })
})