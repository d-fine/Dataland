<template>
  <span data-test="proxied-companies-cell">
    <template v-for="(company, index) in companies" :key="company.companyId">
      <a
        v-if="company.linkable"
        :href="`/companies/${company.companyId}`"
        class="proxied-company-link"
        data-test="proxied-company-link"
        @click.prevent="$emit('navigate', company.companyId)"
        >{{ company.companyName }}</a
      >
      <span v-else data-test="proxied-company-text">{{ company.companyName }}</span
      ><template v-if="index < companies.length - 1">, </template>
    </template>
  </span>
</template>

<script setup lang="ts">
/**
 * A company proxied by the company of the table row. Companies that are not part of the portfolio
 * are not linkable.
 */
export interface ProxiedCompany {
  companyId: string;
  companyName: string;
  linkable: boolean;
}

defineProps<{
  companies: ProxiedCompany[];
}>();

defineEmits<{
  navigate: [companyId: string];
}>();
</script>

<style scoped>
.proxied-company-link {
  color: var(--p-primary-color);
  text-decoration: none;
  cursor: pointer;
}

.proxied-company-link:hover {
  text-decoration: underline;
}
</style>
