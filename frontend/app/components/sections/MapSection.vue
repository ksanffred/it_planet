<script setup lang="ts">
type YMaps3Instance = {
  ready: Promise<void>
  YMap: new (container: HTMLElement, props: unknown) => YMapInstance
  YMapDefaultSchemeLayer: new (props?: unknown) => unknown
  YMapDefaultFeaturesLayer: new (props?: unknown) => unknown
}

type YMapInstance = {
  addChild: (child: unknown) => unknown
  destroy?: () => void
}

const mapContainerRef = ref<HTMLElement | null>(null)
const mapError = ref('')
let mapInstance: YMapInstance | null = null

const waitForYMaps = (timeoutMs = 10000, intervalMs = 120) =>
  new Promise<YMaps3Instance>((resolve, reject) => {
    const start = Date.now()
    const timerId = window.setInterval(() => {
      const ymaps3 = (window as Window & { ymaps3?: YMaps3Instance }).ymaps3
      if (ymaps3) {
        window.clearInterval(timerId)
        resolve(ymaps3)
        return
      }

      if (Date.now() - start >= timeoutMs) {
        window.clearInterval(timerId)
        reject(new Error('Yandex Maps script is not loaded'))
      }
    }, intervalMs)
  })

const initMap = async () => {
  if (!mapContainerRef.value) return

  try {
    const ymaps3 = await waitForYMaps()
    await ymaps3.ready

    mapInstance = new ymaps3.YMap(mapContainerRef.value, {
      location: {
        center: [37.617635, 55.755814],
        zoom: 10,
      },
    })

    mapInstance.addChild(new ymaps3.YMapDefaultSchemeLayer())
    mapInstance.addChild(new ymaps3.YMapDefaultFeaturesLayer())
  } catch (error) {
    mapError.value = 'Не удалось загрузить карту'
    console.error('Failed to initialize yandex map', error)
  }
}

onMounted(initMap)

onUnmounted(() => {
  mapInstance?.destroy?.()
  mapInstance = null
})
</script>

<template>
  <section class="map-section">
    <div ref="mapContainerRef" class="map-section__canvas bordered" />
    <div v-if="mapError" class="map-section__error">
      {{ mapError }}
    </div>
  </section>
</template>

<style scoped lang="scss">
.map-section {
  position: relative;
  margin-top: 20px;

  &__canvas {
    width: 100%;
    height: 620px;
    border-radius: 24px;
    overflow: hidden;
  }

  &__error {
    position: absolute;
    top: 16px;
    left: 16px;
    padding: 8px 12px;
    border-radius: 12px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .map-section__canvas {
    height: 440px;
  }
}
</style>
