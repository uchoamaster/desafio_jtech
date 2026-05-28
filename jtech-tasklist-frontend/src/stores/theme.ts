import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

type ThemeMode = 'light' | 'dark'

const STORAGE_KEY = 'jtech-tasklist.theme'

function readThemeMode(): ThemeMode {
  if (typeof window === 'undefined') {
    return 'dark'
  }

  const storedMode = window.localStorage.getItem(STORAGE_KEY)

  if (storedMode === 'light' || storedMode === 'dark') {
    return storedMode
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>(readThemeMode())
  const isDark = computed(() => mode.value === 'dark')
  const currentThemeName = computed(() => (isDark.value ? 'jtechDark' : 'jtechLight'))

  function persist() {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(STORAGE_KEY, mode.value)
  }

  function toggleTheme() {
    mode.value = isDark.value ? 'light' : 'dark'
    persist()
  }

  function setTheme(nextMode: ThemeMode) {
    mode.value = nextMode
    persist()
  }

  return {
    currentThemeName,
    isDark,
    mode,
    setTheme,
    toggleTheme,
  }
})