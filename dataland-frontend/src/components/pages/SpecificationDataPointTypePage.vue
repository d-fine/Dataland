<template>
  <TheContent class="flex">
    <div class="col-12 text-left pb-0">
      <nav class="specification-breadcrumb text-color-secondary mb-2">
        <router-link to="/specifications" class="text-primary underline">Specifications</router-link>
      </nav>
      <div v-if="isPending">
        <p class="font-medium text-xl">Loading data point type...</p>
        <DatalandProgressSpinner />
      </div>
      <FailMessage v-else-if="isError" message="Could not load this data point type." />
      <template v-else-if="dataPointType">
        <h1>{{ dataPointType.name }}</h1>
        <p class="text-lg">{{ dataPointType.businessDefinition }}</p>
      </template>
    </div>
    <div v-if="dataPointType" class="grid m-0 text-left">
      <div class="col-12 md:col-6">
        <h2>Base type</h2>
        <router-link
          :to="`/specifications/data-point-base-types/${dataPointType.dataPointBaseType.id}`"
          class="text-primary font-semibold underline"
        >
          {{ dataPointType.dataPointBaseType.id }}
        </router-link>
      </div>

      <div v-if="dataPointType.constraints?.length" class="col-12 md:col-6">
        <h2>Constraints</h2>
        <ul>
          <li v-for="constraint in dataPointType.constraints" :key="constraint">{{ constraint }}</li>
        </ul>
      </div>

      <div v-if="dataPointType.calculationRules.length" class="col-12">
        <h2>Calculation rules</h2>
        <ul>
          <li v-for="(rule, index) in dataPointType.calculationRules" :key="index">
            {{ rule.calculationMethod }} (inputs: {{ rule.inputs.join(', ') }})
          </li>
        </ul>
      </div>

      <div v-if="dataPointType.usedBy.length" class="col-12">
        <h2>Used by these frameworks</h2>
        <ul>
          <li v-for="usage in dataPointType.usedBy" :key="usage.id">
            <router-link :to="`/specifications/frameworks/${usage.id}`" class="text-primary font-semibold underline">
              {{ usage.id }}
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { toRef } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { useDataPointTypeSpecificationQuery } from '@/api-queries/specification/useDataPointTypeSpecificationQuery.ts';

const props = defineProps<{
  dataPointTypeId: string;
}>();

const {
  data: dataPointType,
  isPending,
  isError,
} = useDataPointTypeSpecificationQuery({ dataPointTypeId: toRef(props, 'dataPointTypeId') });
</script>

<style scoped>
.specification-breadcrumb {
  font-size: var(--font-size-sm);
}
</style>
