// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    'oxc-nuxt',
    '@nuxt/test-utils/module',
    '@nuxtjs/color-mode',
    '@nuxt/image',
    '@nuxt/icon',
  ],
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
  image: {
    format: ['webp'],
    quality: 80,
  },
  icon: {
    componentName: 'NuxtIcon',
  },
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080',
    },
  },
})
