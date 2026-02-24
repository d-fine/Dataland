<template>
  <div class="card p-0 overflow-hidden">
    <div class="p-datatable p-component">
      <div class="p-datatable-wrapper overflow-auto">
        <table class="p-datatable-table w-full" aria-label="Dataset review comparison table">
          <thead class="p-datatable-thead">
            <tr class="border-bottom-table">
              <th class="horizontal-headers-size">
                <div class="p-column-header-content">
                  <span class="p-column-title">KPI Name</span>
                </div>
              </th>
              <th class="horizontal-headers-size">
                <div class="p-column-header-content">
                  <span class="p-column-title">
                    Original Datapoint
                    <span class="block text-xs font-normal">Data extractor company</span>
                  </span>
                </div>
              </th>
              <th class="horizontal-headers-size">
                <div class="p-column-header-content">
                  <span class="p-column-title">
                    Corrected Datapoint
                    <span class="block text-xs font-normal">Quality Assurance company</span>
                  </span>
                </div>
              </th>
              <th class="horizontal-headers-size">
                <div class="p-column-header-content">
                  <span class="p-column-title">Custom Datapoint</span>
                </div>
              </th>
            </tr>
          </thead>

          <tbody>
            <!-- Loading / error for original dataset -->
            <tr v-if="loadingOriginal">
              <td colspan="5" class="p-3 text-center">
                <p class="font-medium text-xl">Loading Dataset..</p>
                <DatalandProgressSpinner />
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
                :class="row.type === 'section' ? 'surface-100 ' : 'border-bottom-1 surface-border hover:surface-50'"
              >
                <!-- Section header row -->
                <template v-if="row.type === 'section'">
                  <td
                    colspan="5"
                    class="text-left p-3 border-bottom-1 surface-border"
                    :class="{
                      'section-root': row.level === 0,
                      'section-sub': row.level > 0,
                    }"
                  >
                    {{ row.level === 0 ? row.label : toTitleCase(row.label) }}
                  </td>
                </template>

                <!-- KPI row -->
                <template v-else>
                  <!-- KPI name column, styled like old table -->
                  <td
                    class="headers-bg vertical-align-top header-column-width"
                    :data-cell-label="row.label"
                    data-row-header="true"
                  >
                    <div class="flex justify-content-between align-items-center">
                      <span
                        class="table-left-label text-primary cursor-pointer font-medium"
                        @click="openJudgeModal(row)"
                      >
                        {{ row.label }}
                      </span>
                      <em
                        v-if="row.explanation"
                        class="material-icons info-icon ml-2"
                        aria-hidden="true"
                        v-tooltip.top="{ value: row.explanation }"
                      >
                        info
                      </em>
                    </div>
                  </td>

                  <!-- Original datapoint -->
                  <td class="vertical-align-top border-right-1 surface-border">
                    <MultiLayerDataTableCell
                      :content="row.originalDisplay"
                      :meta-info="dataMetaInformation as DataMetaInformation"
                      :inReviewMode="true"
                    />
                  </td>

                  <!-- Corrected datapoint -->
                  <td class="vertical-align-top border-right-1 surface-border">
                    <span v-if="getReviewInfo(row.dataPointTypeId)?.acceptedSource === 'Qa'">
                      {{ getCorrectedDisplay(getReviewInfo(row.dataPointTypeId)) ?? 'QA Accepted' }}
                    </span>
                    <span v-else-if="getReviewInfo(row.dataPointTypeId)?.qaReport" class="text-color-secondary italic">
                      QA suggestion: {{ getCorrectedDisplay(getReviewInfo(row.dataPointTypeId)) ?? '—' }}
                    </span>
                    <span v-else class="text-color-secondary italic"> No correction </span>
                  </td>

                  <!-- Custom datapoint -->
                  <td class="vertical-align-top border-right-1 surface-border">
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
    </div>
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
import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import Tooltip from 'primevue/tooltip';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';

const props = defineProps<{
  companyId: string;
  framework: DataTypeEnum | string;
  dataId: string;
  searchQuery: string;
  datasetReview: DatasetReviewOverview;
  dataMetaInformation: DataMetaInformation;
}>();

const apiClientProvider = useApiClient();
const vTooltip = Tooltip;

const {
  data: originalDataAndMeta,
  isPending: loadingOriginal,
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
  level: number;
};

type CellRow = {
  type: 'cell';
  label: string;
  dataPointTypeId?: string;
  originalDisplay: AvailableMLDTDisplayObjectTypes;
  explanation?: string;
};

type KpiRow = SectionRow | CellRow;

// Recursively build rows from MLDT config + one dataset
function buildRowsFromConfig(config: MLDTConfig<FrameworkData>, data: FrameworkData, level = 0): KpiRow[] {
  const rows: KpiRow[] = [];
  for (const item of config) {
    if (item.type === 'section') {
      const section = item as MLDTSectionConfig<FrameworkData>;
      rows.push({ type: 'section', label: section.label, level: level });
      rows.push(...buildRowsFromConfig(section.children, data, level + 1));
    } else if (item.type === 'cell') {
      const cell = item as MLDTCellConfig<FrameworkData>;
      rows.push({
        type: 'cell',
        label: cell.label,
        dataPointTypeId: cell.dataPointTypeId,
        originalDisplay: cell.valueGetter(data),
        explanation: cell.explanation,
      });
    }
  }
  return rows;
}

const allRows = computed<KpiRow[]>(() => {
  if (!originalDataAndMeta.value || !mldtConfig.value) return [];
  const rows = buildRowsFromConfig(mldtConfig.value, originalDataAndMeta.value.data);
  console.log(
    'KPI rows in review table:',
    rows.filter((r) => r.type === 'cell').map((r) => (r as CellRow).label)
  );
  return rows;
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

const toTitleCase = (str: string) => {
  return str
    .toLowerCase()
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};
</script>

<style scoped>
.info-icon {
  display: inline-flex;
  align-items: center;
  cursor: help;
}

.section-root {
  /* top-level section headers (e.g. ENVIRONMENTAL, SOCIAL) */
  text-transform: uppercase;
  font-size: var(--font-size-base); /* slightly larger than cell text */
  font-weight: var(--font-weight-medium); /* not full bold */
}

.section-sub {
  /* nested sections (e.g. Greenhouse Gas Emissions) */
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-normal);
}
</style>
