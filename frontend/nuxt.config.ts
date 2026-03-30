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
      yandexMapsApiKey: '',
    },
  },
  app: {
    head: {
      script: [
        {
          src: 'https://api-maps.yandex.ru/v3/?apikey=071323d3-8c9a-48c3-9baf-5c437af0f80e&lang=ru_RU',
          type: 'text/javascript',
        },
      ],
    },
  },
})
