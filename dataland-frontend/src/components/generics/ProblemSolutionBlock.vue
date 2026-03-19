<template>
  <div class="problem-solution-block" role="group" :aria-label="`${problemTitle} and ${solutionTitle}`">
    <div class="problem-solution-block__problem">
      <h3 class="problem-solution-block__title">{{ problemTitle }}</h3>
      <p class="problem-solution-block__text">{{ problemText }}</p>
    </div>
    <div class="problem-solution-block__arrow" aria-hidden="true">
      <img :src="'/static/images/arrow_big.svg'" alt="" class="problem-solution-block__arrow-img" />
    </div>
    <div class="problem-solution-block__solution">
      <h3 class="problem-solution-block__title problem-solution-block__title--accent">{{ solutionTitle }}</h3>
      <p class="problem-solution-block__text">{{ solutionText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  problemTitle: string;
  problemText: string;
  solutionTitle: string;
  solutionText: string;
}>();
</script>

<style scoped lang="scss">
.problem-solution-block {
  display: grid;
  grid-template-columns: 40% 20% 40%;
  align-items: start;
  gap: 0;
  max-width: 1100px;
  margin: 0 auto;

  &__problem,
  &__solution {
    padding: 16px;
  }

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0 0 8px;
    color: var(--p-text-color, #1b1b1b);

    &--accent {
      color: var(--p-primary-color);
    }
  }

  &__text {
    font-size: 1rem;
    line-height: 1.6;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
  }

  &__arrow {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px 0;
  }

  &__arrow-img {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }
}

@media only screen and (max-width: $bp-lg) {
  .problem-solution-block {
    grid-template-columns: 1fr;
    gap: 8px;

    &__arrow {
      padding: 8px 0;
    }

    &__arrow-img {
      transform: rotate(90deg);
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .problem-solution-block {
    &__arrow {
      display: none;
    }

    &__problem {
      border-bottom: 1px solid var(--p-surface-200, #e6e6e6);
      padding-bottom: 16px;
    }

    &__solution {
      padding-top: 16px;
    }
  }
}
</style>
