import { defineComponent, h } from 'vue'
import { vi } from 'vitest'

function simpleWrapper(tag = 'div') {
  return defineComponent({
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h(tag, attrs, slots.default?.())
    },
  })
}

const VTextField = defineComponent({
  name: 'VTextField',
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    label: {
      type: String,
      default: '',
    },
    type: {
      type: String,
      default: 'text',
    },
    placeholder: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['update:modelValue', 'keyup.enter'],
  setup(props, { attrs, emit }) {
    return () =>
      h('label', { class: 'stub-field' }, [
        props.label ? h('span', props.label) : null,
        h('input', {
          ...attrs,
          'aria-label': props.label,
          disabled: props.disabled,
          placeholder: props.placeholder,
          type: props.type,
          value: props.modelValue,
          onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLInputElement).value),
          onKeyup: (event: KeyboardEvent) => {
            if (event.key === 'Enter') {
              emit('keyup.enter', event)
            }
          },
        }),
      ])
  },
})

const VTextarea = defineComponent({
  name: 'VTextarea',
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    label: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['update:modelValue'],
  setup(props, { attrs, emit }) {
    return () =>
      h('label', { class: 'stub-field' }, [
        props.label ? h('span', props.label) : null,
        h('textarea', {
          ...attrs,
          'aria-label': props.label,
          disabled: props.disabled,
          placeholder: props.placeholder,
          value: props.modelValue,
          onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLTextAreaElement).value),
        }),
      ])
  },
})

const VBtn = defineComponent({
  name: 'VBtn',
  inheritAttrs: false,
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: 'button',
    },
  },
  emits: ['click'],
  setup(props, { attrs, emit, slots }) {
    return () =>
      h(
        'button',
        {
          ...attrs,
          disabled: props.disabled,
          type: props.type,
          onClick: (event: MouseEvent) => emit('click', event),
        },
        slots.default?.(),
      )
  },
})

const VForm = defineComponent({
  name: 'VForm',
  inheritAttrs: false,
  emits: ['submit'],
  setup(_, { attrs, emit, slots }) {
    return () =>
      h(
        'form',
        {
          ...attrs,
          onSubmit: (event: Event) => {
            event.preventDefault()
            emit('submit', event)
          },
        },
        slots.default?.(),
      )
  },
})

const VAlert = defineComponent({
  name: 'VAlert',
  inheritAttrs: false,
  setup(_, { attrs, slots }) {
    return () => h('div', { ...attrs, role: 'alert' }, slots.default?.())
  },
})

export function installMatchMediaMock() {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
      matches: query.includes('dark'),
      media: query,
      onchange: null,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  })
}

export const componentStubs = {
  VAlert,
  VApp: simpleWrapper(),
  VAppBar: simpleWrapper('header'),
  VAvatar: simpleWrapper(),
  VBtn,
  VCard: simpleWrapper(),
  VCardText: simpleWrapper(),
  VChip: simpleWrapper(),
  VCol: simpleWrapper(),
  VContainer: simpleWrapper(),
  VDivider: simpleWrapper('hr'),
  VForm,
  VMain: simpleWrapper('main'),
  VRow: simpleWrapper(),
  VSheet: simpleWrapper(),
  VTextField,
  VTextarea,
  TransitionGroup: simpleWrapper(),
}