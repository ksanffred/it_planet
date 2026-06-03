# Документация фронтенда — Tramplin (трамплин)

Платформа для связи IT-соискателей с работодателями. Соискатели могут просматривать стажировки, вакансии, менторство и ивенты; работодатели — размещать возможности и управлять откликами.

---

## Стек технологий

| Технология         | Версия  | Назначение                     |
| ------------------ | ------- | ------------------------------ |
| Nuxt               | ^4.4.2  | SSR/SSG мета-фреймворк         |
| Vue                | ^3.5.30 | UI фреймворк                   |
| vue-router         | ^5.0.3  | Клиентская маршрутизация       |
| TypeScript         | ^5.9.3  | Типизация                      |
| SCSS (sass)        | ^1.98.0 | Препроцессор стилей            |
| Yandex Maps 3.0    | —       | Гео-сервисы (карта, геокодинг) |
| @nuxt/icon         | 2.2.1   | Иконки (Material Symbols)      |
| @nuxt/image        | 2.0.0   | Оптимизация изображений        |
| @nuxtjs/color-mode | 4.0.0   | Тёмная/светлая тема            |
| Vitest             | ^4.1.0  | Тест-раннер                    |
| Oxlint             | ^1.56.0 | Линтер                         |
| Oxfmt              | ^0.41.0 | Форматтер                      |
| Playwright         | ^1.58.2 | E2E тесты                      |
| Yarn Berry         | 4.13.0  | Пакетный менеджер              |

---

## Структура проекта

```
frontend/
├── app/                          # Исходный код приложения (Nuxt 4 app directory)
│   ├── app.vue                   # Корневой компонент
│   ├── assets/
│   │   └── styles/               # Глобальные SCSS стили
│   │       ├── main.scss         # Точка входа стилей
│   │       ├── _normalize.scss   # CSS reset
│   │       ├── _fonts.scss       # @font-face (Plus Jakarta Sans, Inter)
│   │       ├── _variables.scss   # CSS-переменные (светлая и тёмная темы)
│   │       ├── _globals.scss     # Базовые стили body, .container
│   │       ├── _utils.scss       # Утилитарные классы
│   │       └── helpers/          # SCSS helpers (breakpoints, mixins)
│   ├── components/
│   │   ├── AppForm.vue           # Карточка-обёртка для форм
│   │   ├── AuthWrapper.vue       # Full-screen обёртка для страниц авторизации
│   │   ├── FormInputField.vue    # Label + Input обёртка
│   │   ├── base/                 # Базовые переиспользуемые компоненты
│   │   │   ├── AppButton.vue     # Кнопка (primary/secondary/bordered)
│   │   │   ├── AppInput.vue      # Текстовое поле
│   │   │   ├── AppLogo.vue       # Логотип «трамплин»
│   │   │   ├── AppTag.vue        # Цветной тег/бейдж
│   │   │   ├── BackButton.vue    # Кнопка «Назад»
│   │   │   ├── OpportunityCard.vue # Карточка возможности
│   │   │   ├── TagSelector.vue   # Выпадающий мульти-селект тегов
│   │   │   └── UserIdentity.vue  # Аватар + имя + подпись
│   │   ├── layout/
│   │   │   ├── AppNav.vue        # Верхняя навигация
│   │   │   └── AppFooter.vue     # Подвал
│   │   └── sections/
│   │       ├── HeroSection.vue   # Герой-секция главной
│   │       ├── MapSection.vue    # Яндекс.Карта с кластеризацией
│   │       ├── OpportunitiesSection.vue # Сетка карточек с пагинацией
│   │       └── SearchSection.vue # Поиск + фильтры + переключатель карты
│   ├── layouts/
│   │   └── default.vue           # Дефолтный лейаут (Nav + slot + Footer)
│   ├── middleware/
│   │   └── guest-only.ts         # Редирект авторизованных с auth-страниц
│   ├── pages/
│   │   ├── index.vue             # / — Главная
│   │   ├── me.vue                # /me — Заглушка профиля
│   │   ├── auth/
│   │   │   ├── login.vue         # /auth/login — Вход
│   │   │   ├── register.vue      # /auth/register — Регистрация
│   │   │   └── verify.vue        # /auth/verify — Верификация email
│   │   ├── applicants/
│   │   │   ├── index.vue         # /applicants — Создание профиля
│   │   │   ├── [id].vue          # /applicants/:id — Просмотр соискателя
│   │   │   └── me.vue            # /applicants/me — Личный кабинет
│   │   ├── employers/
│   │   │   ├── register.vue      # /employers/register — Регистрация компании
│   │   │   ├── [id].vue          # /employers/:id — Просмотр работодателя
│   │   │   └── me.vue            # /employers/me — Кабинет работодателя
│   │   └── opportunities/
│   │       ├── create.vue        # /opportunities/create — Создание возможности
│   │       └── [id].vue          # /opportunities/:id — Детали возможности
│   ├── types/                    # TypeScript интерфейсы
│   │   ├── index.ts              # Реэкспорт всех типов
│   │   ├── auth.ts               # UserRole, LoginRequest, AuthResponse и др.
│   │   ├── applicant.ts          # Applicant, CreateApplicantRequest и др.
│   │   ├── employer.ts           # Employer, RegisterEmployerRequest и др.
│   │   ├── opportunity.ts        # OpportunityType, OpportunityCard и др.
│   │   ├── tag.ts                # Tag, TagCategory, TagResponse
│   │   ├── contact.ts            # ApplicantContact, ApplicantContactStatus
│   │   ├── favorite.ts           # FavoriteOpportunity, AddFavoritesBulkRequest
│   │   ├── response.ts           # OpportunityResponse, OpportunityResponseStatus
│   │   ├── recommendation.ts     # Recommendation, RecommendationResponse
│   │   ├── curator.ts            # Curator, CuratorResponse
│   │   ├── media.ts              # MediaUploadResponse
│   │   └── error.ts              # ApiError
│   └── utils/
│       ├── formatOpportunityFormat.ts # Форматирование OpportunityFormat
│       ├── formatOpportunityType.ts   # Форматирование OpportunityType
│       └── normalizeStorageAssetUrl.ts # Нормализация S3/CDN URL
├── public/                       # Статика (шрифты, изображения, data/)
├── test/                         # Тесты
├── nuxt.config.ts                # Конфигурация Nuxt
├── tsconfig.json                 # Конфигурация TypeScript
├── vitest.config.ts              # Конфигурация Vitest
├── oxlint.config.ts              # Конфигурация Oxlint
├── .oxfmtrc.json                 # Конфигурация Oxfmt
├── .yarnrc.yml                   # Конфигурация Yarn Berry
├── Dockerfile                    # Docker-сборка
└── package.json                  # Зависимости и скрипты
```

---

## Страницы и маршруты

| Маршрут                 | Файл                             | Описание                                                     | Лейаут  | Middleware |
| ----------------------- | -------------------------------- | ------------------------------------------------------------ | ------- | ---------- |
| `/`                     | `pages/index.vue`                | Главная (герой + поиск + карта/список)                       | default | —          |
| `/me`                   | `pages/me.vue`                   | Заглушка профиля                                             | default | —          |
| `/auth/login`           | `pages/auth/login.vue`           | Вход (email + пароль)                                        | false   | guest-only |
| `/auth/register`        | `pages/auth/register.vue`        | Регистрация (роль APPLICANT/EMPLOYER)                        | false   | guest-only |
| `/auth/verify`          | `pages/auth/verify.vue`          | Верификация email по токену                                  | false   | —          |
| `/applicants`           | `pages/applicants/index.vue`     | Создание профиля соискателя                                  | default | —          |
| `/applicants/me`        | `pages/applicants/me.vue`        | ЛК соискателя (аватар, резюме, контакты, избранное, отклики) | default | —          |
| `/applicants/:id`       | `pages/applicants/[id].vue`      | Публичный профиль соискателя                                 | default | —          |
| `/employers/register`   | `pages/employers/register.vue`   | Регистрация работодателя (данные компании)                   | default | —          |
| `/employers/me`         | `pages/employers/me.vue`         | ЛК работодателя (возможности, отклики)                       | default | —          |
| `/employers/:id`        | `pages/employers/[id].vue`       | Профиль работодателя                                         | default | —          |
| `/opportunities/create` | `pages/opportunities/create.vue` | Создание возможности                                         | default | —          |
| `/opportunities/:id`    | `pages/opportunities/[id].vue`   | Детальный просмотр возможности                               | default | —          |

---

## Компоненты

### Базовые (base/)

| Компонент         | Входные параметры                                                                              | Описание                       |
| ----------------- | ---------------------------------------------------------------------------------------------- | ------------------------------ |
| `AppButton`       | `label`, `type` (button/submit), `variant` (primary/secondary/bordered), `disabled`, `loading` | Переиспользуемая кнопка        |
| `AppInput`        | `type` (text/email/password/number/url), `placeholder`, `modelValue`                           | Текстовое поле ввода           |
| `AppLogo`         | `dot` (показывать точку), `tagline` (показывать слоган)                                        | Логотип платформы              |
| `AppTag`          | `tag` (объект с `id`, `name`, `color`)                                                         | Цветной тег                    |
| `BackButton`      | —                                                                                              | Кнопка возврата на главную     |
| `OpportunityCard` | `opportunity` (объект), `favoriteIds` (Set), `onToggleFavorite`                                | Карточка возможности           |
| `TagSelector`     | `tags` (массив), `selectedIds` (Set), `onToggle`, `placeholder`                                | Выпадающий мульти-селект тегов |
| `UserIdentity`    | `src` (URL аватара), `name`, `subtitle`                                                        | Аватар + имя + подпись         |

### Составные

| Компонент        | Описание                                                               |
| ---------------- | ---------------------------------------------------------------------- |
| `AuthWrapper`    | Full-screen обёртка для страниц входа/регистрации с тематическим фоном |
| `AppForm`        | Карточка с логотипом, заголовком, описанием и slot для содержимого     |
| `FormInputField` | Обёртка `label + AppInput` для полей форм                              |

### Секции (sections/)

| Компонент              | Описание                                                                     |
| ---------------------- | ---------------------------------------------------------------------------- |
| `HeroSection`          | Герой-блок главной (заголовок, описание, теги возможностей, арт)             |
| `SearchSection`        | Поисковая строка, фильтры по типу, выбор тегов, переключатель карты/списка   |
| `OpportunitiesSection` | Сетка `OpportunityCard` с пагинацией и поиском, подгрузка из API             |
| `MapSection`           | Яндекс.Карта с кастомными маркерами, кластеризацией, popup и боковой панелью |

### Лейаут (layout/)

| Компонент   | Описание                                                             |
| ----------- | -------------------------------------------------------------------- |
| `AppNav`    | Верхняя навигация: логотип, переключатель темы, кнопки входа/профиля |
| `AppFooter` | Подвал с ссылками и копирайтом                                       |

---

## Модели данных (типы)

### Auth (`types/auth.ts`)

```typescript
type UserRole = 'APPLICANT' | 'EMPLOYER'

interface LoginRequest {
  email: string
  password: string
}

interface RegisterRequest {
  email: string
  password: string
  displayName: string
  role: UserRole
}

interface AuthResponse {
  token: string
  user: CurrentUserResponse
}

interface CurrentUserResponse {
  id: string
  email: string
  displayName: string
  role: UserRole
  profile: Applicant | Employer | null
}
```

### Applicant (`types/applicant.ts`)

```typescript
interface Applicant {
  id: string
  userId: string
  university: string | null
  faculty: string | null
  currentFieldOfStudy: string | null
  graduationYear: number | null
  portfolio: string | null
  additionalEducation: string | null
  tags: Tag[]
  isProfileVisible: boolean
  avatarUrl: string | null
  resumeUrl: string | null
  createdAt: string
  updatedAt: string
}
```

### Employer (`types/employer.ts`)

```typescript
interface Employer {
  id: string
  userId: string
  companyName: string
  description: string
  inn: string
  website: string | null
  socials: string | null
  logoUrl: string | null
  createdAt: string
  updatedAt: string
}
```

### Opportunity (`types/opportunity.ts`)

```typescript
type OpportunityType = 'VACANCY' | 'INTERNSHIP' | 'MENTORSHIP' | 'EVENT'
type OpportunityFormat = 'REMOTE' | 'OFFICE' | 'HYBRID'
type OpportunityStatus = 'ACTIVE' | 'CLOSED' | 'DRAFT'

interface OpportunityCard {
  id: string
  employerId: string
  title: string
  description: string
  type: OpportunityType
  format: OpportunityFormat
  city: string | null
  address: string | null
  salaryMin: number | null
  salaryMax: number | null
  expiresAt: string | null
  status: OpportunityStatus
  tags: Tag[]
  mediaUrls: string[]
  lat: number | null
  lng: number | null
  employer: EmployerResponse
  createdAt: string
  updatedAt: string
}
```

---

## API эндпоинты

Базовый URL: `http://localhost:8080` (настраивается в `nuxt.config.ts` → `runtimeConfig.public.apiBase`)

### Аутентификация

| Метод | Эндпоинт              | Описание                            |
| ----- | --------------------- | ----------------------------------- |
| POST  | `/auth/login`         | Вход (возвращает JWT + user)        |
| POST  | `/auth/register`      | Регистрация (возвращает JWT + user) |
| GET   | `/auth/verify?token=` | Верификация email                   |

### Соискатели (Applicants)

| Метод | Эндпоинт                                   | Описание                      |
| ----- | ------------------------------------------ | ----------------------------- |
| GET   | `/applicants/me`                           | Мой профиль соискателя        |
| POST  | `/applicants`                              | Создать профиль соискателя    |
| GET   | `/applicants/{id}`                         | Профиль соискателя по ID      |
| PUT   | `/applicants/me/visibility`                | Переключить видимость профиля |
| POST  | `/applicants/{id}/avatar`                  | Загрузить аватар              |
| POST  | `/applicants/{id}/resume`                  | Загрузить резюме (PDF)        |
| GET   | `/applicants/me/contacts`                  | Мои контакты                  |
| POST  | `/applicants/me/contacts/{id}`             | Добавить контакт              |
| GET   | `/applicants/{id}/contacts`                | Контакты соискателя           |
| GET   | `/applicants/{id}/favorites/opportunities` | Избранные возможности         |
| GET   | `/applicants/{id}/responses`               | Отклики соискателя            |

### Работодатели (Employers)

| Метод | Эндпоинт              | Описание                   |
| ----- | --------------------- | -------------------------- |
| GET   | `/employers/me`       | Мой профиль работодателя   |
| GET   | `/employers/{id}`     | Профиль работодателя по ID |
| POST  | `/employers/register` | Зарегистрировать компанию  |

### Возможности (Opportunities)

| Метод | Эндпоинт                            | Описание                       |
| ----- | ----------------------------------- | ------------------------------ |
| GET   | `/opportunities/mini-cards?search=` | Список возможностей            |
| GET   | `/opportunities/{id}`               | Детали возможности             |
| POST  | `/opportunities`                    | Создать возможность            |
| GET   | `/opportunities/me`                 | Мои возможности (работодатель) |
| POST  | `/opportunities/{id}/responses`     | Откликнуться на возможность    |
| GET   | `/opportunities/responses/me`       | Мои отклики (соискатель)       |
| GET   | `/opportunities/responses/employer` | Отклики на мои возможности     |

### Избранное (Favorites)

| Метод  | Эндпоинт                                      | Описание              |
| ------ | --------------------------------------------- | --------------------- |
| GET    | `/applicants/me/favorites/opportunities/{id}` | Проверить избранное   |
| POST   | `/applicants/me/favorites/opportunities/{id}` | Добавить в избранное  |
| DELETE | `/applicants/me/favorites/opportunities/{id}` | Удалить из избранного |

### Теги

| Метод | Эндпоинт | Описание          |
| ----- | -------- | ----------------- |
| GET   | `/tags`  | Список всех тегов |

### Внешние API

| Сервис                | Описание                                                              |
| --------------------- | --------------------------------------------------------------------- |
| Яндекс.Геокодирование | Преобразование адреса в координаты (lat/lng) при создании возможности |
| Яндекс.Карты 3.0      | Отображение кластеризованных маркеров на главной                      |

---

## Управление состоянием

В проекте **нет** глобального store (Pinia/Vuex). Используются:

| Механизм                  | Где используется                                                                 |
| ------------------------- | -------------------------------------------------------------------------------- |
| `useCookie()`             | Хранение `auth_token` (JWT, 7 дней) и `user_data` (id, email, displayName, role) |
| `localStorage`            | Избранное для неавторизованных пользователей (`opportunity-favorite-ids`)        |
| URL query params          | Параметры поиска (`?search=`), режим отображения (`?view=map/list`)              |
| `ref()` / `computed()`    | Локальное состояние в компонентах                                                |
| `useFetch()` / `$fetch()` | Данные с API (хранятся локально в компонентах)                                   |

---

## Стилизация и темизация

### SCSS архитектура

```
app/assets/styles/
├── main.scss           # Точка входа (импортирует все partials)
├── _normalize.scss     # Сброс стилей
├── _fonts.scss         # Подключение шрифтов (WOFF2)
├── _variables.scss     # CSS custom properties (светлая и тёмная темы)
├── _globals.scss       # body, .container
├── _utils.scss         # .reset-button, .reset-link, .bordered и др.
└── helpers/
    ├── _breakpoints.scss   # Брейкпоинты (телефон → 4K)
    ├── _mixins.scss        # Миксины
    └── _functions.scss     # Функции (пока пусто)
```

### Темы

Переключение через `@nuxtjs/color-mode`:

- Режимы: `light` (по умолчанию), `dark`, `system` (следует за системой)
- CSS-переменные в `_variables.scss` для `html.light` и `html.dark`
- Переключатель в `AppNav.vue` (иконка солнца/луны)

Ключевые цвета:

| Переменная              | Светлая тема | Тёмная тема |
| ----------------------- | ------------ | ----------- |
| `--primary-color`       | `#0049b8`    | `#1e3a8a`   |
| `--secondary-color`     | `#ff5f00`    | `#f97316`   |
| `--tertiary-color`      | `#558b6e`    | `#14532d`   |
| `--background-color`    | `#fcfaee`    | `#0b0f14`   |
| `--text-inverted-color` | `#161413`    | `#f8fafc`   |

Шрифты: `Plus Jakarta Sans` (ExtraBold 800, заголовки) и `Inter` (Medium/SemiBold/Bold, текст).

Утилитарный класс `.container`: `max-width: 1180px; margin-inline: auto;`

---

## Скрипты (package.json)

| Команда             | Описание                                |
| ------------------- | --------------------------------------- |
| `npm run dev`       | Запуск dev-сервера (hot-reload)         |
| `npm run build`     | Production сборка                       |
| `npm run generate`  | Статическая генерация                   |
| `npm run preview`   | Предпросмотр production сборки          |
| `npm run lint`      | Линтинг (Oxlint с type-aware проверкой) |
| `npm run format`    | Форматирование (Oxfmt)                  |
| `npm run test`      | Все тесты                               |
| `npm run test:unit` | Unit-тесты (Vitest, проект `unit`)      |
| `npm run test:e2e`  | E2E-тесты (Vitest, проект `e2e`)        |
| `npm run test:nuxt` | Nuxt-тесты (Vitest, проект `nuxt`)      |

---

## Заметки по архитектуре

- **Нет сервисного слоя** — вызовы API делаются напрямую в компонентах через `$fetch`/`useFetch`.
- **Избранное** — dual-режим: `localStorage` для анонимов, API для авторизованных. Идентификация возможностей при отсутствии ID — через сигнатуру `title|company|type`.
- **Язык** — весь UI на русском, утилиты `formatOpportunityFormat`/`formatOpportunityType` используются только для CSS-классов, не для текста.
- **Валидация email** — при регистрации EMPLOYER проверяется, что домен не входит в список публичных почтовых сервисов (файл `public/media/data/domains.json`).
