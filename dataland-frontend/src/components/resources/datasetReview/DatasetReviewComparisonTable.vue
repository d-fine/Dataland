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

          <th class="w-1"></th>
        </tr>
      </thead>

      <tbody>
        <!-- Loading / error for original dataset -->
        <tr v-if="loadingOriginal">
          <td colspan="5" class="p-3 text-center">
            <i class="pi pi-spin pi-spinner" style="font-size: 1.5rem"></i>
          </td>
        </tr>
        <tr v-else-if="errorOriginal">
          <td colspan="5" class="p-3 text-center text-red-500">Failed to load original dataset</td>
        </tr>

        <!-- Section + KPI rows -->
        <template v-else>
          <tr
            v-for="(row, index) in filteredRows"
            :key="row.type + '-' + row.label + '-' + index"
            :class="
              row.type === 'section' ? 'surface-100 font-bold' : 'border-bottom-1 surface-border hover:surface-50'
            "
          >
            <!-- Section header row -->
            <template v-if="row.type === 'section'">
              <td colspan="5" class="p-3 border-bottom-1 surface-border">
                {{ row.label.toUpperCase() }}
              </td>
            </template>

            <!-- KPI row -->
            <template v-else>
              <!-- KPI name -->
              <td class="p-3 border-right-1 surface-border">
                <span class="text-primary cursor-pointer font-medium" @click="openJudgeModal(row)">
                  {{ row.label }}
                </span>
              </td>

              <!-- Original datapoint -->
              <td class="p-3 border-right-1 surface-border">
                <MultiLayerDataTableCell
                  :content="row.originalDisplay"
                  :meta-info="dataMetaInformation as DataMetaInformation"
                  :inReviewMode="true"
                />
              </td>

              <!-- Corrected datapoint -->
              <td class="p-3 border-right-1 surface-border bg-orange-50">
                <span v-if="getReviewInfo(row.dataPointTypeId)?.acceptedSource === 'Qa'">
                  {{ getCorrectedDisplay(getReviewInfo(row.dataPointTypeId)) ?? 'QA Accepted' }}
                </span>
                <span v-else-if="getReviewInfo(row.dataPointTypeId)?.qaReport" class="text-color-secondary italic">
                  QA suggestion: {{ getCorrectedDisplay(getReviewInfo(row.dataPointTypeId)) ?? '—' }}
                </span>
                <span v-else class="text-color-secondary italic"> No correction </span>
              </td>

              <!-- Custom datapoint -->
              <td class="p-3 border-right-1 surface-border">
                <span v-if="getReviewInfo(row.dataPointTypeId)?.acceptedSource === 'Custom'">
                  {{ getReviewInfo(row.dataPointTypeId)?.customValue ?? '-' }}
                </span>
                <span v-else>-</span>
              </td>

              <!-- Icon column (very simple first pass) -->
              <td class="p-3 text-center">
                <i
                  v-if="getReviewInfo(row.dataPointTypeId)?.acceptedSource"
                  class="pi pi-check-circle text-xl text-green-500"
                />
              </td>
            </template>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query';

defineOptions({ name: 'DatasetReviewComparisonTable' });

import { computed } from 'vue';
import MultiLayerDataTableCell from '@/components/resources/dataTable/MultiLayerDataTableCell.vue';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import type {
  MLDTConfig,
  MLDTCellConfig,
  MLDTSectionConfig,
} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import type { AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import type { DataMetaInformation, DataTypeEnum } from '@clients/backend';
import type { DatasetReviewOverview, DataPointReviewInfo } from '@/utils/DatasetReviewOverview.ts';
import { useApiClient } from '@/utils/useApiClient.ts';

const props = defineProps<{
  companyId: string;
  framework: DataTypeEnum | string;
  dataId: string;
  searchQuery: string;
  datasetReview: DatasetReviewOverview;
  dataMetaInformation: DataMetaInformation;
}>();

const apiClientProvider = useApiClient();

const {
  data: originalDataAndMeta,
  isLoading: loadingOriginal,
  error: errorOriginal,
} = useQuery({
  queryKey: ['frameworkData', props.framework, props.dataId],
  queryFn: async () => {
    const api = getFrameworkDataApiForIdentifier(props.framework, apiClientProvider);
    if (!api) {
      throw new Error(`No data API for ${props.framework}`);
    }
    const response = await api.getFrameworkData(props.dataId);
    return response.data;
  },
  enabled: !!props.framework && !!props.dataId,
});

// --- Get MLDT config for this framework (view configuration) ---
const frameworkDefinition = computed(() => getFrontendFrameworkDefinition(props.framework as DataTypeEnum));
const viewConfig = computed(() => frameworkDefinition.value?.getFrameworkViewConfiguration());
const mldtConfig = computed<MLDTConfig<FrameworkData> | undefined>(
  () => viewConfig.value?.configuration as MLDTConfig<FrameworkData> | undefined
);

// --- Row model ---
type SectionRow = {
  type: 'section';
  label: string;
};

type CellRow = {
  type: 'cell';
  label: string;
  dataPointTypeId?: string;
  originalDisplay: AvailableMLDTDisplayObjectTypes;
};

type KpiRow = SectionRow | CellRow;

// Recursively build rows from MLDT config + one dataset
function buildRowsFromConfig(config: MLDTConfig<FrameworkData>, data: FrameworkData): KpiRow[] {
  const rows: KpiRow[] = [];
  for (const item of config) {
    if (item.type === 'section') {
      const section = item as MLDTSectionConfig<FrameworkData>;
      rows.push({ type: 'section', label: section.label });
      rows.push(...buildRowsFromConfig(section.children, data));
    } else if (item.type === 'cell') {
      const cell = item as MLDTCellConfig<FrameworkData>;
      rows.push({
        type: 'cell',
        label: cell.label,
        dataPointTypeId: cell.dataPointTypeId,
        originalDisplay: cell.valueGetter(data),
      });
    }
  }
  return rows;
}

const allRows = computed<KpiRow[]>(() => {
  if (!originalDataAndMeta.value || !mldtConfig.value) return [];
  return buildRowsFromConfig(mldtConfig.value, originalDataAndMeta.value.data);
});

const filteredRows = computed<KpiRow[]>(() => {
  if (!props.searchQuery) return allRows.value;
  const q = props.searchQuery.toLowerCase();
  return allRows.value.filter((row) => (row.type === 'section' ? true : row.label.toLowerCase().includes(q)));
});

// --- Helpers to join review info ---
function getReviewInfo(dataPointTypeId?: string): DataPointReviewInfo | undefined {
  if (!dataPointTypeId) return undefined;
  return props.datasetReview.dataPoints[dataPointTypeId];
}

function getCorrectedDisplay(dp: DataPointReviewInfo | undefined): string | null {
  if (!dp?.qaReport?.correctedData) return null;
  try {
    const parsed = JSON.parse(dp.qaReport.correctedData as string);
    // Many of your correctedData examples are { value, quality, ... }
    return parsed.value == null ? null : String(parsed.value);
  } catch {
    return null;
  }
}

function openJudgeModal(row: KpiRow): void {
  if (row.type === 'section') return;
  console.log('open judge modal for', row.dataPointTypeId, row.label);
  // TODO: integrate judge modal implementation in follow-up ticket
}
</script>

<style scoped>
th {
  font-size: 0.875rem;
}
td {
  font-size: 0.875rem;
}
</style>
