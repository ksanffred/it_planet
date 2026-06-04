export default defineNuxtRouteMiddleware(() => {
  const tokenCookie = useCookie<string | null>('auth_token')

  if (tokenCookie.value) {
    const userCookie = useCookie<string | null>('user_data')
    let role = ''
    if (userCookie.value) {
      try {
        role = JSON.parse(userCookie.value).role
      } catch {}
    }
    if (role === 'EMPLOYER') {
      return navigateTo('/employers/me')
    }
    return navigateTo('/applicants/me')
  }
})
