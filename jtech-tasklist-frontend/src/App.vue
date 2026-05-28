<script setup lang="ts">
import { watchEffect } from 'vue'
import { RouterView } from 'vue-router'
import { useTheme } from 'vuetify'

import { useThemeStore } from '@/stores/theme'

const theme = useTheme()
const themeStore = useThemeStore()

watchEffect(() => {
  theme.global.name.value = themeStore.currentThemeName

  if (typeof document !== 'undefined') {
    document.documentElement.dataset.theme = themeStore.mode
  }
})
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <Transition mode="out-in" name="route-fade-slide">
      <component :is="Component" :key="route.fullPath" />
    </Transition>
  </RouterView>
</template>

<style scoped>
.route-fade-slide-enter-active,
.route-fade-slide-leave-active {
  transition:
    opacity 0.28s ease,
    transform 0.28s ease,
    filter 0.28s ease;
}

.route-fade-slide-enter-from,
.route-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(18px) scale(0.985);
  filter: blur(8px);
}
</style>
