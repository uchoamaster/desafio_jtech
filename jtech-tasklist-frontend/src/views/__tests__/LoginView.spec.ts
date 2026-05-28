import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

import LoginView from '@/views/LoginView.vue'
import { useAuthStore } from '@/stores/auth'
import { installTestStorage } from '@/stores/__tests__/testStorage'
import { componentStubs, installMatchMediaMock } from '@/test-utils/componentStubs'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

describe('LoginView', () => {
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    installTestStorage().clear()
    installMatchMediaMock()
    pinia = createPinia()
    setActivePinia(pinia)
    pushMock.mockReset()
  })

  it('shows an authentication error when login fails', async () => {
    const authStore = useAuthStore()
    vi.spyOn(authStore, 'login').mockResolvedValue({ ok: false, error: 'Credenciais invalidas' })

    const wrapper = mount(LoginView, {
      global: {
        plugins: [pinia],
        stubs: componentStubs,
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('angelo')
    await inputs[1].setValue('123456')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(authStore.login).toHaveBeenCalledWith('angelo', '123456')
    expect(wrapper.get('[role="alert"]').text()).toContain('Credenciais invalidas')
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('navigates to the dashboard after successful login', async () => {
    const authStore = useAuthStore()
    vi.spyOn(authStore, 'login').mockResolvedValue({ ok: true })

    const wrapper = mount(LoginView, {
      global: {
        plugins: [pinia],
        stubs: componentStubs,
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('angelo')
    await inputs[1].setValue('123456')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(pushMock).toHaveBeenCalledWith({ name: 'dashboard' })
  })
})