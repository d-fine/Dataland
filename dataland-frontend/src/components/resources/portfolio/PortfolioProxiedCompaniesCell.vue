<template>
  <span data-test="proxied-companies-cell">
    <template v-for="(company, index) in companies" :key="company.companyId">
      <a
        :href="`/companies/${company.companyId}`"
        class="proxied-company-link"
        data-test="proxied-company-link"
        @click.prevent="$emit('navigate', company.companyId)"
        >{{ company.companyName }}</a
      ><template v-if="index < companies.length - 1">, </template>
    </template>
  </span>
</template>

<script setup lang="ts">
/**
 * A company proxied by the company of the table row. Every proxied company links to its company page.
 */
export interface ProxiedCompany {
  companyId: string;
  companyName: string;
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
