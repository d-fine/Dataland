<template>
  <span v-tooltip.top="tooltipOptions" data-test="proxied-companies-cell">
    <template v-for="(company, index) in visibleCompanies" :key="company.companyId">
      <Button
        v-if="company.linkable"
        :label="company.companyName"
        variant="link"
        data-test="proxied-company-link"
        @click="$emit('navigate', company.companyId)"
        :pt="buttonPt"
      />
      <span v-else data-test="proxied-company-text">{{ company.companyName }}</span
      ><template v-if="index < visibleCompanies.length - 1">, </template>
    </template>
    <template v-if="isTruncated">…</template>
  </span>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import Tooltip from 'primevue/tooltip';
import { computed } from 'vue';

const vTooltip = Tooltip;

/**
 * A company proxied by the company of the table row. Companies that are not part of the portfolio
 * are not linkable.
 */
export interface ProxiedCompany {
  companyId: string;
  companyName: string;
  linkable: boolean;
}

const MAX_PROXIED_COMPANIES_CHARACTERS = 60;

const SEPARATOR = ', ';

const props = defineProps<{
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

const fullText = computed(() => props.companies.map((company) => company.companyName).join(SEPARATOR));

const isTruncated = computed(() => fullText.value.length > MAX_PROXIED_COMPANIES_CHARACTERS);

const visibleCompanies = computed(() => {
  if (!isTruncated.value) return props.companies;
  const visible: ProxiedCompany[] = [];
  let length = 0;
  for (const company of props.companies) {
    const added = company.companyName.length + (visible.length > 0 ? SEPARATOR.length : 0);
    if (visible.length > 0 && length + added > MAX_PROXIED_COMPANIES_CHARACTERS) break;
    visible.push(company);
    length += added;
  }
  return visible;
});

const tooltipOptions = computed(() => (isTruncated.value ? { value: fullText.value } : undefined));
</script>
