<template>
  <router-link
    v-if="specificationExists"
    :to="`/specifications/frameworks/${frameworkIdentifier}`"
    class="specification-link text-primary font-semibold"
    data-test="frameworkSpecificationLink"
  >
    <i class="pi pi-book" aria-hidden="true" />
    View specification &amp; documentation
  </router-link>
</template>

<script setup lang="ts">
import { toRef } from 'vue';
import { useFrameworkSpecificationExistsQuery } from '@/api-queries/specification/useFrameworkSpecificationExistsQuery.ts';

const props = defineProps<{
  /**
   * The Dataland framework identifier (e.g. `sfdr`, `eutaxonomy-financials`) to link the specification of.
   */
  frameworkIdentifier: string;
}>();

const { data: specificationExists } = useFrameworkSpecificationExistsQuery({
  frameworkId: toRef(props, 'frameworkIdentifier'),
});
</script>

<style scoped>
.specification-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xxs);
  font-size: var(--font-size-sm);
  text-decoration: underline;
}
</style>
