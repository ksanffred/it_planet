// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: ['oxc-nuxt', '@nuxt/test-utils/module', '@nuxtjs/color-mode'],
  devtools: { enabled: true },
  compatibilityDate: '2025-07-15',
  vite: {
    css: {
      preprocessorMaxWorkers: true,
    },
  },
  css: ['~/assets/styles/main.scss'],
  colorMode: {
    preference: 'system',
    fallback: 'light',
  },
})
