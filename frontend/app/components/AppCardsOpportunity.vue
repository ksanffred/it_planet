<script setup lang="ts">
interface Opportunity {
  id: number
  title: string
  company: string
  type: 'internship' | 'vacancy' | 'mentorship' | 'event'
}

const opportunities: Opportunity[] = [
  { id: 1, title: 'Frontend Intern', company: 'TechCorp', type: 'internship' },
  { id: 2, title: 'Backend Developer', company: 'StartupX', type: 'vacancy' },
  { id: 3, title: 'Python Mentor', company: 'CodeAcademy', type: 'mentorship' },
  { id: 4, title: 'Career Workshop', company: 'IT Hub', type: 'event' },
  { id: 5, title: 'React Intern', company: 'WebStudio', type: 'internship' },
  { id: 6, title: 'DevOps Engineer', company: 'CloudTeam', type: 'vacancy' },
  { id: 7, title: 'JS Mentor', company: 'LearnCode', type: 'mentorship' },
  { id: 8, title: 'Tech Conference', company: 'EventPro', type: 'event' },
  { id: 9, title: 'Fullstack Intern', company: 'DigitalLab', type: 'internship' },
]
</script>

<template>
  <div class="app-cards-opportunity container">
    <div class="app-cards-opportunity__grid">
      <div v-for="item in opportunities" :key="item.id" class="app-cards-opportunity__card">
        <h3 class="app-cards-opportunity__card-title">{{ item.title }}</h3>
        <p class="app-cards-opportunity__card-company">{{ item.company }}</p>
        <span class="app-cards-opportunity__card-type">{{ item.type }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.app-cards-opportunity {
  padding: 24px 0;

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  &__card {
    background-color: var(--background-secondary-color);
    border-radius: 16px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__card-title {
    font-size: 18px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 700;
  }

  &__card-company {
    font-size: 14px;
    font-family: 'Inter', sans-serif;
    font-weight: 500;
    color: var(--text-secondary-color);
  }

  &__card-type {
    font-size: 12px;
    font-family: 'Inter', sans-serif;
    font-weight: 600;
    text-transform: uppercase;
    color: var(--primary-color);
  }
}
</style>
