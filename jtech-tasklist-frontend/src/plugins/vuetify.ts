import 'vuetify/styles'

import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'jtechDark',
    themes: {
      jtechDark: {
        dark: true,
        colors: {
          background: '#0d1518',
          surface: '#152126',
          'surface-bright': '#22343b',
          primary: '#f2bd62',
          secondary: '#74d5c0',
          success: '#8dd9a2',
          warning: '#f2bd62',
          error: '#ef7f74',
          info: '#7db6ff',
        },
      },
      jtechLight: {
        dark: false,
        colors: {
          background: '#f4efe7',
          surface: '#fffdf9',
          'surface-bright': '#ebe4d8',
          primary: '#a8602f',
          secondary: '#2d7f72',
          success: '#4d9a64',
          warning: '#c78f28',
          error: '#c85d51',
          info: '#4473c5',
        },
      },
    },
  },
  defaults: {
    VCard: {
      rounded: 'xl',
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
    },
    VTextarea: {
      variant: 'outlined',
      density: 'comfortable',
    },
    VBtn: {
      rounded: 'pill',
    },
  },
})