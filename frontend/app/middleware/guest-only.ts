export default defineNuxtRouteMiddleware(() => {
  const tokenCookie = useCookie<string | null>('auth_token')

  if (tokenCookie.value) {
    return navigateTo('/applicants/me')
  }
})
