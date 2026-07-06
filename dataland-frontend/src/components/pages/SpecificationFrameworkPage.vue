<template>
  <TheContent class="flex">
    <div class="col-12 text-left pb-0">
      <nav class="specification-breadcrumb text-color-secondary mb-2">
        <router-link to="/specifications" class="text-primary underline">Specifications</router-link>
        <span v-if="framework"> / {{ framework.name }}</span>
      </nav>
      <div v-if="isPending">
        <p class="font-medium text-xl">Loading framework specification...</p>
        <DatalandProgressSpinner />
      </div>
      <FailMessage v-else-if="isError" message="Could not load this framework specification." />
      <template v-else-if="framework">
        <h1>{{ framework.name }}</h1>
        <p class="text-lg">{{ framework.businessDefinition }}</p>
      </template>
    </div>
    <div v-if="framework" class="grid m-0 text-left">
      <div class="col-12">
        <h2>Fields</h2>
        <p class="text-color-secondary">
          Click on a field to see its data point definition, validation rules and where else it is used.
        </p>
        <SpecificationSchemaTree :schema="parsedSchema" />
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { computed, toRef } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import SpecificationSchemaTree from '@/components/resources/specifications/SpecificationSchemaTree.vue';
import { useFrameworkSpecificationQuery } from '@/api-queries/specification/useFrameworkSpecificationQuery.ts';

const props = defineProps<{
  frameworkId: string;
}>();

const {
  data: framework,
  isPending,
  isError,
} = useFrameworkSpecificationQuery({ frameworkId: toRef(props, 'frameworkId') });

const parsedSchema = computed<Record<string, unknown>>(() => {
  if (!framework.value) {
    return {};
  }
  try {
    return JSON.parse(framework.value.schema) as Record<string, unknown>;
  } catch {
    return {};
  }
});
</script>

<style scoped>
.specification-breadcrumb {
  font-size: var(--font-size-sm);
}
</style>
