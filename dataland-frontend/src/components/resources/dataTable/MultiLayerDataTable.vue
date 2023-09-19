<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper" style="overflow: auto">
      <table class="p-datatable-table w-full">
        <thead class="p-datatable-thead" style="position: sticky">
          <tr>
            <th class="horizontal-headers-size">
              <div class="p-column-header-content">
                <span class="p-column-title">KPIs</span>
              </div>
            </th>
            <th v-for="(dataset, idx) in datasets" :key="idx" class="horizontal-headers-size">
              <div class="p-column-header-content">
                <span class="p-column-title">{{ dataset.headerLabel }}</span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="p-datatable-tbody">
          <MultiLayerDataTableBody :datasets="datasets" :config="config" :is-top-level="true" />
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
import { tr } from "@faker-js/faker";

defineProps<{
  config: MLDTConfig<T>;
  datasets: Array<MLDTDataset<T>>;
}>();
</script>
