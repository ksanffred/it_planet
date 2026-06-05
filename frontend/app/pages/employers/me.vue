<script lang="ts" setup>
import { normalizeStorageAssetUrl } from "~/utils/normalizeStorageAssetUrl";

type EmployerProfileMe = {
  id: number;
  userId: number;
  companyName: string;
  description?: string;
  inn: string;
  website?: string;
  socials?: string;
  logoUrl?: string;
  verifiedOrgName?: string;
  status: string;
};

type EmployerOpportunityPosting = {
  id: number;
  title: string;
  status: "ACTIVE" | "CLOSED" | "PLANNED" | string;
  type: string;
  published_at?: string;
  expires_at?: string;
  applications_count: number;
};

type EmployerOpportunityApplicationItem = {
  applicant_id: number;
  applicant_name: string;
  university?: string;
  desired_position?: string;
  recommendation: number;
  matching_tags?: Array<{
    id: number;
    name: string;
    category: string;
  }>;
};

const config = useRuntimeConfig();
const tokenCookie = useCookie<string | null>("auth_token");

if (!tokenCookie.value) {
  await navigateTo("/auth/login?redirect=/employers/me");
}

const authHeaders = {
  Authorization: `Bearer ${tokenCookie.value}`,
};

const opportunitiesSearch = ref("");
const responsesSearch = ref("");

const {
  data: employer,
  pending: employerPending,
  error: employerError,
} = await useFetch<EmployerProfileMe>("/employers/me", {
  baseURL: config.public.apiBase,
  method: "GET",
  headers: authHeaders,
});

const { data: opportunities } = await useFetch<EmployerOpportunityPosting[]>(
  "/opportunities/me",
  {
    baseURL: config.public.apiBase,
    method: "GET",
    headers: authHeaders,
    default: () => [],
  },
);

const { data: responses } = await useFetch<
  EmployerOpportunityApplicationItem[]
>("/opportunities/responses/employer", {
  baseURL: config.public.apiBase,
  method: "GET",
  headers: authHeaders,
  default: () => [],
});

watchEffect(() => {
  if (employerError.value?.statusCode === 404) {
    navigateTo("/employers/register");
  } else if (employerError.value?.statusCode === 401) {
    navigateTo("/auth/login?redirect=/employers/me");
  } else if (!employerPending.value && !employer.value) {
    navigateTo("/employers/register");
  }
});

const employerInitials = computed(() => {
  const source = employer.value?.companyName?.trim();
  if (!source) return "??";

  const parts = source.split(/\s+/).filter(Boolean);
  return parts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
});

const employerLogoUrl = computed(() =>
  normalizeStorageAssetUrl(String(employer.value?.logoUrl ?? "")),
);

const profileDescription = computed(
  () => employer.value?.description || "Описание компании не заполнено",
);
const profileLinks = computed(() => {
  const website = employer.value?.website;
  const socials = employer.value?.socials;
  return [website, socials].filter(Boolean).join(" • ") || "Ссылки не указаны";
});

const filteredOpportunities = computed(() => {
  const query = opportunitiesSearch.value.trim().toLowerCase();
  if (!query) return opportunities.value ?? [];

  return (opportunities.value ?? []).filter((item) =>
    `${item.title} ${item.type} ${item.status}`.toLowerCase().includes(query),
  );
});

const filteredResponses = computed(() => {
  const query = responsesSearch.value.trim().toLowerCase();
  if (!query) return responses.value ?? [];

  return (responses.value ?? []).filter((item) =>
    `${item.applicant_name} ${item.desired_position} ${item.university}`
      .toLowerCase()
      .includes(query),
  );
});

const opportunityStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: "Активна",
    CLOSED: "Закрыто",
    PLANNED: "Запланировано",
  };
  return map[status] ?? status;
};

const opportunityStatusClass = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: "active",
    CLOSED: "closed",
    PLANNED: "planned",
  };
  return map[status] ?? "default";
};

const opportunityStatusExtra = (item: EmployerOpportunityPosting) => {
  if (!item.expires_at) return "";
  const date = new Date(item.expires_at);
  if (Number.isNaN(date.getTime())) return "";
  return `до ${date.toLocaleDateString("ru-RU")}`;
};

type EditSection = "description" | "links" | "logo" | null;
const activeModal = ref<EditSection>(null);
const editDescription = ref("");
const editWebsite = ref("");
const editSocials = ref("");
const editLogoUrl = ref("");
const logoMode = ref<"file" | "url">("url");
const logoFile = ref<File | null>(null);
const logoFilePreview = ref("");
const isLogoUploading = ref(false);
const logoUploadError = ref("");
const logoFileInput = ref<HTMLInputElement | null>(null);
const isSavingSection = ref(false);
const sectionSaveError = ref("");

const logoSaveText = computed(() =>
  logoMode.value === "file" ? "Загрузить" : "Сохранить",
);

const openEditDescription = () => {
  editDescription.value = employer.value?.description ?? "";
  activeModal.value = "description";
};

const openEditLinks = () => {
  editWebsite.value = employer.value?.website ?? "";
  editSocials.value = employer.value?.socials ?? "";
  activeModal.value = "links";
};

const openEditLogo = () => {
  editLogoUrl.value = employer.value?.logoUrl ?? "";
  logoMode.value = "url";
  logoFile.value = null;
  logoFilePreview.value = "";
  logoUploadError.value = "";
  activeModal.value = "logo";
};

const handleLogoFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    logoUploadError.value = "Можно загрузить только изображение";
    input.value = "";
    return;
  }

  logoFile.value = file;
  logoFilePreview.value = URL.createObjectURL(file);
  logoUploadError.value = "";
};

const uploadLogoFile = async () => {
  if (!logoFile.value || !employer.value) return;

  isLogoUploading.value = true;
  logoUploadError.value = "";

  try {
    const formData = new FormData();
    formData.append("file", logoFile.value);

    const response = await $fetch<{ path?: string; url?: string }>(
      `/employers/${employer.value.id}/logo`,
      {
        baseURL: config.public.apiBase,
        method: "POST",
        headers: authHeaders,
        body: formData,
      },
    );

    const nextLogoUrl = normalizeStorageAssetUrl(
      response.url ?? response.path ?? "",
    );
    if (nextLogoUrl && employer.value) {
      employer.value = { ...employer.value, logoUrl: nextLogoUrl };
    }

    activeModal.value = null;
  } catch (err) {
    logoUploadError.value = "Не удалось загрузить логотип";
    console.error("Failed to upload logo", err);
  } finally {
    isLogoUploading.value = false;
  }
};

const confirmLogoSave = () => {
  if (logoMode.value === "file" && logoFile.value) {
    uploadLogoFile();
  } else {
    saveSection();
  }
};

const saveSection = async () => {
  if (!employer.value || isSavingSection.value || !activeModal.value) return;
  isSavingSection.value = true;
  sectionSaveError.value = "";

  const body: Record<string, unknown> = {
    description: employer.value.description ?? null,
    website: employer.value.website ?? null,
    socials: employer.value.socials ?? null,
    logoUrl: employer.value.logoUrl ?? null,
  };
  switch (activeModal.value) {
    case "description":
      body.description = editDescription.value || null;
      break;
    case "links":
      body.website = editWebsite.value || null;
      body.socials = editSocials.value || null;
      break;
    case "logo":
      body.logoUrl = editLogoUrl.value || null;
      break;
  }

  try {
    const updated = await $fetch<EmployerProfileMe>("/employers/me", {
      baseURL: config.public.apiBase,
      method: "PUT",
      headers: authHeaders,
      body,
    });
    employer.value = updated;
    activeModal.value = null;
  } catch (err) {
    sectionSaveError.value = "Не удалось сохранить изменения";
    console.error("Failed to save employer section", err);
  } finally {
    isSavingSection.value = false;
  }
};

const closeSectionModal = () => {
  activeModal.value = null;
  sectionSaveError.value = "";
};

const showLogoutModal = ref(false);

const handleLogout = () => {
  tokenCookie.value = null;
  const userCookie = useCookie<string | null>("user_data");
  userCookie.value = null;
  navigateTo("/auth/login");
};
</script>

<template>
  <div class="employer-cabinet container">
    <header class="employer-cabinet__header">
      <h1 class="employer-cabinet__title">Личный кабинет</h1>
      <BaseBackButton />
    </header>

    <section class="employer-cabinet__profile bordered">
      <div class="employer-cabinet__profile-top">
        <div class="employer-cabinet__identity">
          <div class="employer-cabinet__avatar-wrap">
            <button
              class="employer-cabinet__avatar"
              type="button"
              @click="openEditLogo"
            >
              <img
                v-if="employerLogoUrl"
                :src="employerLogoUrl"
                alt="Логотип компании"
                class="employer-cabinet__avatar-image"
              />
              <span v-else>{{ employerInitials }}</span>
              <span class="employer-cabinet__avatar-overlay">
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </span>
            </button>
          </div>
          <div class="employer-cabinet__identity-text">
            <p class="employer-cabinet__name">
              {{ employer?.companyName || "Компания" }}
            </p>
            <p class="employer-cabinet__subtitle">Профиль компании</p>
          </div>
        </div>
      </div>

      <div class="employer-cabinet__profile-fields">
        <article class="employer-cabinet__profile-field bordered">
          <div class="employer-cabinet__profile-field-head">
            <h3 class="employer-cabinet__profile-field-title">Описание</h3>
            <button
              class="employer-cabinet__edit-btn"
              type="button"
              @click="openEditDescription"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="employer-cabinet__profile-field-text">
            {{ profileDescription }}
          </p>
        </article>

        <article class="employer-cabinet__profile-field bordered">
          <div class="employer-cabinet__profile-field-head">
            <h3 class="employer-cabinet__profile-field-title">Ссылки</h3>
            <button
              class="employer-cabinet__edit-btn"
              type="button"
              @click="openEditLinks"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="employer-cabinet__profile-field-text">{{ profileLinks }}</p>
        </article>

        <article class="employer-cabinet__profile-field bordered">
          <h3 class="employer-cabinet__profile-field-title">Адрес</h3>
          <p class="employer-cabinet__profile-field-text">Не указан</p>
        </article>
      </div>
    </section>

    <section class="employer-cabinet__columns">
      <article class="employer-cabinet__column bordered">
        <div class="employer-cabinet__column-head">
          <h2 class="employer-cabinet__section-title">Возможности</h2>
          <BaseAppButton
            type="button"
            variant="primary"
            class="employer-cabinet__new-btn"
            @click="navigateTo('/opportunities/create')"
          >
            Новая возможность
          </BaseAppButton>
        </div>
        <BaseAppInput
          v-model="opportunitiesSearch"
          placeholder="Поиск по возможностям"
        />

        <div class="employer-cabinet__list">
          <div
            v-for="item in filteredOpportunities"
            :key="item.id"
            class="employer-cabinet__list-item bordered"
          >
            <div>
              <p class="employer-cabinet__item-title">{{ item.title }}</p>
              <p class="employer-cabinet__item-subtitle">
                {{ item.applications_count }} откликов
              </p>
            </div>
            <span
              :class="[
                'employer-cabinet__badge',
                `employer-cabinet__badge--${opportunityStatusClass(item.status)}`,
              ]"
            >
              {{ opportunityStatusLabel(item.status) }}
              <template v-if="opportunityStatusExtra(item)">
                {{ ` ${opportunityStatusExtra(item)}` }}
              </template>
            </span>
          </div>
          <p
            v-if="!filteredOpportunities.length"
            class="employer-cabinet__muted"
          >
            Возможностей пока нет
          </p>
        </div>
      </article>

      <article class="employer-cabinet__column bordered">
        <h2 class="employer-cabinet__section-title">Отклики на возможности</h2>
        <p class="employer-cabinet__section-caption">
          Показываем отклики по выбранной возможности. Снимите выбор, чтобы
          увидеть всех кандидатов.
        </p>
        <BaseAppInput
          v-model="responsesSearch"
          placeholder="Поиск по откликам выбранной возможности"
        />

        <div class="employer-cabinet__list">
          <div
            v-for="item in filteredResponses"
            :key="`${item.applicant_id}-${item.applicant_name}`"
            class="employer-cabinet__list-item bordered"
          >
            <div>
              <p class="employer-cabinet__item-title">
                {{ item.applicant_name }}
              </p>
              <p class="employer-cabinet__item-subtitle">
                {{ item.desired_position || "Позиция не указана" }} ·
                {{ item.recommendation }} рекомендаций
              </p>
            </div>
            <NuxtLink
              class="employer-cabinet__open-link"
              :to="`/applicants/${item.applicant_id}`"
            >
              Открыть
            </NuxtLink>
          </div>
          <p v-if="!filteredResponses.length" class="employer-cabinet__muted">
            Откликов пока нет
          </p>
        </div>
      </article>
    </section>

    <p v-if="sectionSaveError" class="employer-cabinet__save-error">
      {{ sectionSaveError }}
    </p>

    <div class="employer-cabinet__logout-row">
      <BaseAppButton
        variant="secondary"
        class="bordered"
        @click="showLogoutModal = true"
      >
        Выйти из профиля
      </BaseAppButton>
    </div>
    <SvgBlockShape class="block-shape" />
    <SvgRingShape class="ring-shape" />
    <SvgBarShape class="bar-shape" />
  </div>

  <BaseAppModal
    :visible="activeModal === 'description'"
    title="Описание компании"
    @confirm="saveSection"
    @cancel="closeSectionModal"
  >
    <FormInputField
      id="edit-description"
      label="Описание"
      v-model="editDescription"
    />
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'links'"
    title="Ссылки"
    @confirm="saveSection"
    @cancel="closeSectionModal"
  >
    <FormInputField
      id="edit-website"
      label="Сайт"
      v-model="editWebsite"
      type="url"
    />
    <FormInputField
      id="edit-socials"
      label="Соцсети/контакты"
      v-model="editSocials"
    />
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'logo'"
    title="Логотип компании"
    :confirm-text="logoSaveText"
    @confirm="confirmLogoSave"
    @cancel="closeSectionModal"
  >
    <div class="employer-cabinet__logo-tabs">
      <button
        type="button"
        :class="[
          'employer-cabinet__logo-tab',
          { 'employer-cabinet__logo-tab--active': logoMode === 'file' },
        ]"
        @click="logoMode = 'file'"
      >
        Загрузить файл
      </button>
      <button
        type="button"
        :class="[
          'employer-cabinet__logo-tab',
          { 'employer-cabinet__logo-tab--active': logoMode === 'url' },
        ]"
        @click="logoMode = 'url'"
      >
        Указать ссылку
      </button>
    </div>

    <template v-if="logoMode === 'file'">
      <button
        type="button"
        class="employer-cabinet__logo-file-btn"
        :disabled="isLogoUploading"
        @click="logoFileInput?.click()"
      >
        {{ logoFile ? "Изменить файл" : "Выбрать изображение" }}
      </button>
      <input
        ref="logoFileInput"
        type="file"
        accept="image/*"
        hidden
        @change="handleLogoFileChange"
      />
      <img
        v-if="logoFilePreview"
        :src="logoFilePreview"
        alt="Предпросмотр"
        class="employer-cabinet__logo-preview"
      />
      <p v-if="isLogoUploading" class="employer-cabinet__muted">Загружаем...</p>
      <p v-if="logoUploadError" class="employer-cabinet__save-error">
        {{ logoUploadError }}
      </p>
    </template>

    <template v-else>
      <FormInputField
        id="edit-logo"
        label="Ссылка на логотип"
        type="url"
        v-model="editLogoUrl"
      />
    </template>
  </BaseAppModal>

  <BaseAppModal
    :visible="showLogoutModal"
    title="Выход из профиля"
    confirm-text="Выйти"
    @confirm="handleLogout"
    @cancel="showLogoutModal = false"
  >
    <p class="employer-cabinet__logout-confirm-text">
      Вы уверены, что хотите выйти?
    </p>
  </BaseAppModal>
</template>

<style lang="scss" scoped>
.employer-cabinet {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 10;
  margin-top: 16px;
  margin-bottom: 24px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__title {
    margin: 0;
    font-family: "Plus Jakarta Sans", sans-serif;
    font-size: 42px;
    line-height: 1;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__profile {
    border-radius: 18px;
    padding: 12px;
    background-color: var(--background-secondary-color);
  }

  &__profile-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    gap: 12px;
  }

  &__identity {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__avatar-wrap {
    position: relative;
  }

  &__avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    border: 1px solid #4f80ff;
    color: #2052d4;
    display: grid;
    place-items: center;
    font-family: "Plus Jakarta Sans", sans-serif;
    font-weight: 800;
    font-size: 16px;
    background: #dce9fa;
    overflow: hidden;
    flex-shrink: 0;
    cursor: pointer;
    padding: 0;
    position: relative;
  }

  &__avatar-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__avatar-overlay {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    display: none;
    place-items: center;
  }

  &__avatar:hover &__avatar-overlay {
    display: grid;
  }

  &__logo-tabs {
    display: flex;
    gap: 0;
    border: 1px solid var(--border-color);
    border-radius: 10px;
    overflow: hidden;
  }

  &__logo-tab {
    flex: 1;
    padding: 8px 12px;
    border: none;
    background: transparent;
    color: var(--text-tertiary-color);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &--active {
      background-color: var(--background-tertiary-color);
      color: var(--text-inverted-color);
    }

    &:not(&--active):hover {
      background-color: var(--background-primary-color);
    }
  }

  &__logo-file-btn {
    padding: 10px 16px;
    border-radius: 10px;
    border: 1px dashed var(--border-color);
    background: transparent;
    color: var(--text-tertiary-color);
    font-size: 14px;
    cursor: pointer;
    text-align: center;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: var(--background-primary-color);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  &__logo-preview {
    width: 100%;
    max-height: 200px;
    object-fit: contain;
    border-radius: 10px;
    border: 1px solid var(--border-color);
  }

  &__identity-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    margin: 0;
    font-size: 34px;
    line-height: 1;
    font-family: "Plus Jakarta Sans", sans-serif;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__subtitle {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
    font-weight: 500;
  }

  &__edit-btn {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: none;
    background: transparent;
    color: var(--text-tertiary-color);
    cursor: pointer;
    display: grid;
    place-items: center;
    flex-shrink: 0;
    transition:
      background-color 0.2s ease,
      color 0.2s ease;

    &:hover {
      background-color: var(--background-tertiary-color);
      color: var(--primary-color);
    }
  }

  &__profile-field-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__profile-fields {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__profile-field {
    background-color: var(--background-primary-color);
    border-radius: 10px;
    padding: 10px 12px;
  }

  &__profile-field-title {
    margin: 0 0 4px;
    font-size: 12px;
    line-height: 1.2;
    font-family: "Plus Jakarta Sans", sans-serif;
    color: var(--text-inverted-color);
    font-weight: 800;
  }

  &__profile-field-text {
    margin: 0;
    font-size: 14px;
    color: var(--text-inverted-color);
  }

  &__save-error {
    margin: 0;
    color: #c74e4e;
    font-size: 12px;
    font-weight: 600;
    text-align: center;
  }

  &__columns {
    display: grid;
    grid-template-columns: 2fr 3fr;
    gap: 12px;
  }

  &__column {
    border-radius: 18px;
    padding: 12px;
    background-color: var(--background-secondary-color);
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__column-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__section-title {
    margin: 0;
    font-family: "Plus Jakarta Sans", sans-serif;
    font-size: 34px;
    font-weight: 800;
    color: var(--text-inverted-color);
    line-height: 1.1;
  }

  &__section-caption {
    margin: 0;
    font-size: 13px;
    color: var(--text-tertiary-color);
    line-height: 1.25;
  }

  &__new-btn {
    border-radius: 999px;
    padding-inline: 14px;
    font-size: 12px;
    white-space: nowrap;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-thumb {
      background: var(--border-color);
      border-radius: 3px;
    }
  }

  &__list-item {
    border-radius: 10px;
    background-color: var(--background-primary-color);
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  &__item-title {
    margin: 0;
    font-size: 16px;
    font-family: "Plus Jakarta Sans", sans-serif;
    font-weight: 700;
    color: var(--text-inverted-color);
    line-height: 1.2;
  }

  &__item-subtitle {
    margin: 0;
    font-size: 14px;
    color: var(--text-tertiary-color);
    line-height: 1.2;
  }

  &__badge {
    padding: 6px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    background-color: var(--background-primary-color);
    text-wrap: nowrap;

    &--active {
      background-color: #0b57c9;
      color: #fff;
      border-color: transparent;
    }

    &--planned {
      background-color: #ede6b5;
      color: #333;
      border-color: transparent;
    }

    &--closed {
      background-color: #f3ece1;
      color: #333;
      border-color: #c9bea8;
    }
  }

  &__open-link {
    border-radius: 999px;
    border: 1px solid var(--border-color);
    padding: 6px 14px;
    text-decoration: none;
    color: var(--text-inverted-color);
    font-size: 12px;
    font-weight: 700;
    white-space: nowrap;
  }

  &__muted {
    margin: 0;
    color: var(--text-tertiary-color);
    font-size: 13px;
  }

  &__logout-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
  }

  &__logout-confirm-text {
    margin: 0;
    font-size: 14px;
    color: var(--text-inverted-color);
  }
}

:global(.dark) .employer-cabinet__profile-field,
:global(.dark) .employer-cabinet__list-item {
  background-color: #1f2633 !important;
}

:global(.light) .employer-cabinet__title,
:global(.light) .employer-cabinet__name,
:global(.light) .employer-cabinet__section-title,
:global(.light) .employer-cabinet__profile-field-title,
:global(.light) .employer-cabinet__profile-field-text,
:global(.light) .employer-cabinet__item-title,
:global(.light) .employer-cabinet__open-link,
:global(.light) .employer-cabinet__badge {
  color: #1f2733 !important;
}

:global(.light) .employer-cabinet__subtitle,
:global(.light) .employer-cabinet__section-caption,
:global(.light) .employer-cabinet__item-subtitle,
:global(.light) .employer-cabinet__muted {
  color: #5a6578 !important;
}

@media (max-width: 1100px) {
  .employer-cabinet {
    &__columns {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .employer-cabinet {
    &__header {
      flex-direction: column;
      align-items: flex-start;
    }

    &__title {
      font-size: 30px;
    }

    &__profile-top {
      flex-direction: column;
      align-items: flex-start;
    }

    &__name {
      font-size: 24px;
    }

    &__section-title {
      font-size: 26px;
    }

    &__column-head {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}

.block-shape {
  position: absolute;
  fill: var(--secondary-color);
  z-index: -1;
  right: -100px;
  top: 300px;
}

.ring-shape {
  position: absolute;
  fill: var(--primary-color);
  z-index: -1;
  bottom: 0;
  left: -100px;
}

.bar-shape {
  position: absolute;
  transform: rotate(66deg);
  fill: var(--background-tertiary-color);
  z-index: -1;
  top: 200px;
  left: -120px;
}
</style>
