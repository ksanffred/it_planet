<script setup lang="ts">
const email: Ref<string> = ref('')
const password: Ref<string> = ref('')

definePageMeta({
  layout: false,
})
</script>

<template>
  <BaseBackButton class="back-button" />
  <AuthWrapper>
    <div class="auth__card bordered">
      <BaseAppLogo class="auth__logo" />
      <hgroup class="auth__title-group">
        <h1 class="auth__title">Вход в аккаунт</h1>
        <p class="auth__description">
          Продолжайте поиск стажировок, вакансий, наставников и карьерных событий.
        </p>
      </hgroup>
      <form class="auth__form">
        <label class="auth__label" for="email">
          Email
          <BaseAppInput
            class="auth__input"
            v-model="email"
            type="email"
            id="email"
            name="email"
            autocomplete
            placeholder="you@example.com"
            required
          />
        </label>
        <label class="auth__label" for="password">
          Пароль
          <BaseAppInput
            class="auth__input"
            v-model="password"
            id="password"
            name="password"
            minlength="8"
            maxlength="2147483647"
            type="password"
            autocomplete
            placeholder="**********"
            required
          />
        </label>
        <NuxtLink class="auth__reset-link" to="/">Забыли пароль?</NuxtLink>
        <BaseAppButton :disabled="!password || !email" type="submit" variant="primary"
          >Войти</BaseAppButton
        >
      </form>
      <p class="auth__registration-link">
        Нет аккаунта? <NuxtLink to="/registration">Зарегестрироваться</NuxtLink>
      </p>
    </div>
  </AuthWrapper>
</template>

<style lang="scss" scoped>
.auth {
  &__label {
    display: flex;
    flex-direction: column;
    gap: 8px;
    font-size: 13px;
    color: var(--text-inverted-color);

    font-weight: 700;
  }

  &__input {
    font-size: 14px;
    font-weight: 500;
  }

  &__reset-link {
    text-decoration: none;
    color: var(--primary-color);
    font-weight: 700;
    font-size: 13px;
  }

  &__title-group {
    color: var(--text-inverted-color);
  }

  &__registration-link {
    color: var(--tertiary-color);
    font-weight: 700;
    font-size: 13px;

    & a {
      color: inherit;
      font-weight: inherit;
      font-size: inherit;
    }
  }

  &__card {
    border-radius: 32px;
    padding: 34px 34px 30px 34px;
    width: 460px;
  }

  &__title-group,
  &__card,
  &__form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__logo {
    color: var(--secondary-color);
    font-size: 16px;

    .dark & {
      color: var(--secondary-color);
    }
  }
}

.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}
</style>
