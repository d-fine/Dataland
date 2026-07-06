<template>
  <TheContent class="flex">
    <div class="col-12 text-left pb-0">
      <nav class="specification-breadcrumb text-color-secondary mb-2">
        <router-link to="/specifications" class="text-primary underline">Specifications</router-link>
      </nav>
      <div v-if="isPending">
        <p class="font-medium text-xl">Loading data point base type...</p>
        <DatalandProgressSpinner />
      </div>
      <FailMessage v-else-if="isError" message="Could not load this data point base type." />
      <template v-else-if="dataPointBaseType">
        <h1>{{ dataPointBaseType.name }}</h1>
        <p class="text-lg">{{ dataPointBaseType.businessDefinition }}</p>
      </template>
    </div>
    <div v-if="dataPointBaseType" class="grid m-0 text-left">
      <div class="col-12">
        <h2>Example</h2>
        <SpecificationJsonPreview :value="dataPointBaseType.example" />
      </div>

      <div v-if="dataPointBaseType.usedBy.length" class="col-12">
        <h2>Used by these data point types</h2>
        <ul>
          <li v-for="usage in dataPointBaseType.usedBy" :key="usage.id">
            <router-link
              :to="`/specifications/data-point-types/${usage.id}`"
              class="text-primary font-semibold underline"
            >
              {{ usage.id }}
            </router-link>
          </li>
        </ul>
      </div>

      <div class="col-12">
        <p class="text-color-secondary text-sm">Validated by: {{ dataPointBaseType.validatedBy }}</p>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { toRef } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import SpecificationJsonPreview from '@/components/resources/specifications/SpecificationJsonPreview.vue';
import { useDataPointBaseTypeSpecificationQuery } from '@/api-queries/specification/useDataPointBaseTypeSpecificationQuery.ts';

const props = defineProps<{
  dataPointBaseTypeId: string;
}>();

const {
  data: dataPointBaseType,
  isPending,
  isError,
} = useDataPointBaseTypeSpecificationQuery({ dataPointBaseTypeId: toRef(props, 'dataPointBaseTypeId') });
</script>

<style scoped>
.specification-breadcrumb {
  font-size: var(--font-size-sm);
}
</style>
