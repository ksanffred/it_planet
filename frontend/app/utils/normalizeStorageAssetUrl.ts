const S3_BASE_URL = 'https://s3.twcstorage.ru/9a293295-ac1b-4248-864a-41fe86909693'
const CDN_BASE_URL = 'https://cdn.tramplin.ru/'
const CDN_BASE_URL_HTTP = 'http://cdn.tramplin.ru/'

export const normalizeStorageAssetUrl = (value: string | undefined | null) => {
  const raw = String(value ?? '').trim()
  if (!raw) return ''
  if (raw.toLowerCase() === 'string') return ''

  if (raw.startsWith('/media/')) {
    return raw
  }

  if (raw.startsWith('media/')) {
    return `/${raw}`
  }

  if (raw.startsWith(S3_BASE_URL) || raw.startsWith('/')) {
    return raw
  }

  if (raw.startsWith(CDN_BASE_URL)) {
    return `${S3_BASE_URL}/${raw.slice(CDN_BASE_URL.length)}`
  }

  if (raw.startsWith(CDN_BASE_URL_HTTP)) {
    return `${S3_BASE_URL}/${raw.slice(CDN_BASE_URL_HTTP.length)}`
  }

  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    return raw
  }

  return `${S3_BASE_URL}/${raw}`
}
