<template>
  <span data-test="proxied-companies-cell">
    <template v-for="(company, index) in companies" :key="company.companyId">
      <Button
        v-if="company.linkable"
        :label="company.companyName"
        variant="link"
        data-test="proxied-company-link"
        @click="$emit('navigate', company.companyId)"
        :pt="buttonPt"
      />
      <span v-else data-test="proxied-company-text">{{ company.companyName }}</span
      ><template v-if="index < companies.length - 1">, </template>
    </template>
  </span>
</template>

<script setup lang="ts">
import Button from 'primevue/button';

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

const buttonPt = {
  label: {
    style: 'font-weight: normal; text-align: left;',
  },
  root: {
    style: 'padding: 0;',
  },
};
</script>
