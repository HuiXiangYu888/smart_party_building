<template>
  <div id="app" :class="{ dark: isDark }">
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'

// Dark mode state
const isDark = ref(false)

const toggleDark = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved === 'dark') {
    isDark.value = true
  } else if (!saved) {
    // Auto detect system preference
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
})

// Provide dark mode to child components
provide('isDark', isDark)
provide('toggleDark', toggleDark)
</script>

<style>
#app {
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
