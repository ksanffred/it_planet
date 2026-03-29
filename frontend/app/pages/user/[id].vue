<script lang="ts" setup>
const route = useRoute()
</script>
<template>
  <div class="user-account container">
    <header class="user-account__header">
      <h1 class="user-account__title">Личный кабинет</h1>
      <BaseBackButton />
    </header>
    <main>
      <div class="user-account__main bordered">
        <section class="user-account__main-top">
          <BaseUserIdentity
            avatar="ДК"
            name="Данил Колбасенко"
            subtitle="Профиль пользователя"
            size="lg"
          />

          <BaseAppButton class="user-account__action-button" variant="primary"
            >Добавить в контакты</BaseAppButton
          >
        </section>
      </div>
    </main>
  </div>
</template>
<style lang="scss" scoped>
.user-account {
  display: flex;
  margin-top: 26px;
  flex-direction: column;
  gap: 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__title {
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 30px;
    color: var(--text-inverted-color);
  }

  &__main {
    padding: 20px;
    border-radius: 24px;

    &-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }

  &__action-button {
    background-color: var(--secondary-color);
  }
}
</style>
