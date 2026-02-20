<template>
  <div class="card p-0 overflow-hidden border-1 surface-border border-round-xl">
    <table class="w-full" style="border-collapse: collapse">
      <thead class="surface-50 border-bottom-1 surface-border">
        <tr>
          <th class="text-left p-3 font-medium text-color-secondary w-4">KPI Name</th>

          <th class="text-left p-3 font-medium text-color-secondary w-2">
            Original Datapoint
            <div class="text-xs font-normal">Data extractor company</div>
          </th>

          <th class="text-left p-3 font-medium text-color-secondary w-2">
            Corrected Datapoint
            <div class="text-xs font-normal">Quality Assurance company</div>
          </th>

          <th class="text-left p-3 font-medium text-color-secondary w-2">Custom Datapoint</th>
        </tr>
      </thead>

      <tbody>
        <tr class="surface-100 font-bold">
          <td colspan="4" class="p-3 border-bottom-1 surface-border">ENVIRONMENTAL</td>
        </tr>

        <tr class="border-bottom-1 surface-border hover:surface-50">
          <td class="p-3 border-right-1 surface-border">
            <span class="text-primary cursor-pointer font-medium" @click="openJudgeModal"> Scope 1 GHG emissions </span>
          </td>

          <td class="p-3 border-right-1 surface-border">
            <MultiLayerDataTableCell
              :content="mockOriginalValue"
              :metaInfo="mockMetaInformation"
              :inReviewMode="true"
            />
          </td>

          <td class="p-3 border-right-1 surface-border bg-orange-50">
            <span class="text-color-secondary italic">No correction</span>
          </td>

          <td class="p-3 border-right-1 surface-border">-</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'DatasetReviewComparisonTable' });

import MultiLayerDataTableCell from '@/components/resources/dataTable/MultiLayerDataTableCell.vue';
import { ref } from 'vue';

import type { DataTypeEnum, QaStatus, DataMetaInformation } from '@clients/backend';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

const openJudgeModal = () => {
  console.log('open judge modal');
};

const mockMetaInformation = ref<DataMetaInformation>({
  dataId: 'mock-data-id',
  companyId: 'mock-company-id',
  dataType: 'sfdr' as DataTypeEnum,
  reportingPeriod: '2023',
  uploadTime: Date.now(),
  qaStatus: 'Pending' as QaStatus,
  currentlyActive: true,
} as DataMetaInformation);

const mockOriginalValue = ref<AvailableMLDTDisplayObjectTypes>({
  displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
  displayValue: '371.54 tonnes',
});
</script>

<style scoped>
th {
  font-size: 0.875rem; /* 14px */
}
td {
  font-size: 0.875rem;
}
</style>
