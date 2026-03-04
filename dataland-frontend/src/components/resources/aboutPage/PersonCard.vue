<template>
  <figure class="person-card">
    <div class="person-card__photo-container">
      <img
        v-if="!imageError"
        :src="person.imagePath"
        :alt="person.name"
        class="person-card__photo"
        @error="imageError = true"
      />
    </div>
    <figcaption class="person-card__content">
      <h3 class="person-card__name">{{ person.name }}</h3>
      <p class="person-card__role">{{ person.role }}</p>
      <p v-if="variant === 'leadership'" data-test="person-bio" class="person-card__bio">
        {{ (person as Person).bio }}
      </p>
      <p v-if="variant === 'advisory'" data-test="person-organisation" class="person-card__organisation">
        {{ (person as AdvisoryPerson).organisation }}
      </p>
    </figcaption>
  </figure>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Person, AdvisoryPerson } from '@/components/resources/aboutPage/aboutContent';

defineProps<{
  person: Person | AdvisoryPerson;
  variant: 'leadership' | 'advisory';
}>();

const imageError = ref(false);
</script>

<style scoped lang="scss">
.person-card {
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;

  &__photo-container {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    background-color: var(--p-surface-100, #f4f4f5);
    flex-shrink: 0;
  }

  &__photo {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  &__name {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__role {
    font-size: 1rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-primary-color, #ff6813);
  }

  &__bio {
    font-size: 0.9375rem;
    font-weight: 400;
    margin: 0.5rem 0 0;
    color: var(--p-text-muted-color, #585858);
    line-height: 1.5;
  }

  &__organisation {
    font-size: 0.9375rem;
    font-weight: 400;
    margin: 0.25rem 0 0;
    color: var(--p-text-muted-color, #585858);
  }
}
</style>
