<template>
  <section class="find-lei" role="region" aria-labelledby="find-lei-heading">
    <div class="find-lei__wrapper">
      <h2 id="find-lei-heading" class="find-lei__heading">Search sustainability data by company name or identifier</h2>
      <div class="find-lei__search">
        <CompaniesOnlySearchBar @select-company="router.push(`/companies/${$event.companyId}`)" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<style scoped lang="scss">
.find-lei {
  padding: 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__search {
    width: 100%;
    max-width: 520px;
  }
}

@media only screen and (max-width: $bp-md) {
  .find-lei {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
