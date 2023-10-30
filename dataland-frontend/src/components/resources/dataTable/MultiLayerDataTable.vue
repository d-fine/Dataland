<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table w-full" :aria-label="ariaLabel">
        <thead class="p-datatable-thead">
          <tr>
            <th class="horizontal-headers-size">
              <div class="p-column-header-content">
                <span class="p-column-title">KPIs</span>
              </div>
            </th>
            <th
              v-for="(mldtDataset, idx) in mldtDatasets"
              :key="idx"
              class="horizontal-headers-size"
              :data-dataset-index="idx"
            >
              <div class="p-column-header-content">
                <span class="p-column-title">{{ mldtDataset.headerLabel }}</span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="p-datatable-tbody">
          <MultiLayerDataTableBody :mldtDatasets="mldtDatasets" :config="config" :isTopLevel="true" :isVisible="true" />
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}
</style>

<script setup lang="ts" generic="T">
import { type MLDTConfig, type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import MultiLayerDataTableBody from "@/components/resources/dataTable/MultiLayerDataTableBody.vue";

defineProps<{
  config: MLDTConfig<T>;
  mldtDatasets: Array<MLDTDataset<T>>;
  ariaLabel?: string;
}>();
</script>
