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
              <!-- dynamic Qa columns depending on number of report companies -->
              <th
                v-for="company in datasetReview.qaReporterCompanies"
                :key="company.reporterCompanyId"
                class="horizontal-headers-size"
              >
                <div class="p-column-header-content">
                  <span class="p-column-title">
                    Corrected Datapoint
                    <span class="block text-xs font-normal">{{ company.reportCompanyName }}</span>
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
                    <div class="cell-flex">
                      <MultiLayerDataTableCell
                        :content="row.originalDisplay"
                        :meta-info="dataMetaInformation as DataMetaInformation"
                        :inReviewMode="true"
                      />
                      <span v-if="isCellEmpty(row, AcceptedDataPointSource.Original)" class="main-text-color">
                        &ndash;
                      </span>
                      <span
                        v-if="isAcceptedSource(row, AcceptedDataPointSource.Original)"
                        class="pi pi-check text-green-500 accepted-check"
                        aria-label="Accepted source"
                      ></span>
                      <span
                        v-else-if="shouldShowRejectedIcon(row, AcceptedDataPointSource.Original)"
                        class="pi pi-times text-red-500 rejected-check"
                        aria-label="Rejected source"
                      ></span>
                    </div>
                  </td>

                  <!-- Corrected datapoint -->
                  <td
                    v-for="company in datasetReview.qaReporterCompanies"
                    :key="company.reporterCompanyId"
                    class="vertical-align-top border-right-1 surface-border"
                  >
                    <div class="cell-flex">
                      <span
                        v-if="
                          getQaReportFor(row, company.reporterCompanyId)?.verdict ===
                          QaReportDataPointVerdict.QaAccepted
                        "
                      >
                        QA Accepted
                      </span>
                      <span v-else-if="getQaReportFor(row, company.reporterCompanyId)" class="main-text-color">
                        {{ getCorrectedDisplayFromQaReport(getQaReportFor(row, company.reporterCompanyId)) ?? '—' }}
                      </span>
                      <span v-else class="main-text-color"> &ndash; </span>
                      <span
                        v-if="isAcceptedSource(row, AcceptedDataPointSource.Qa, company.reporterCompanyId)"
                        class="pi pi-check text-green-500 accepted-check"
                        aria-label="Accepted source"
                      ></span>
                      <span
                        v-else-if="shouldShowRejectedIcon(row, AcceptedDataPointSource.Qa, company.reporterCompanyId)"
                        class="pi pi-times text-red-500 rejected-check"
                        aria-label="Rejected source"
                      ></span>
                    </div>
                  </td>

                  <!-- Icon column (very simple first pass) -->
                  <td class="vertical-align-top border-right-1 surface-border">
                    <div class="cell-flex">
                      <span
                        v-if="getReviewInfo(row.dataPointTypeId)?.acceptedSource === AcceptedDataPointSource.Custom"
                      >
                        {{ getReviewInfo(row.dataPointTypeId)?.customValue ?? '-' }}
                      </span>
                      <span
                        v-if="isAcceptedSource(row, AcceptedDataPointSource.Custom)"
                        class="pi pi-check text-green-500 accepted-check"
                        aria-label="Accepted source"
                      ></span>
                      <span
                        v-else-if="shouldShowRejectedIcon(row, AcceptedDataPointSource.Custom)"
                        class="pi pi-times text-red-500 rejected-check"
                        aria-label="Rejected source"
                      ></span>
                    </div>
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
import { computed } from 'vue';
import MultiLayerDataTableCell from '@/components/resources/dataTable/MultiLayerDataTableCell.vue';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import type { MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import type { AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import type { DataMetaInformation, DataTypeEnum } from '@clients/backend';
import {
  type DatasetReviewResponse,
  type DataPointReviewDetails,
  type QaReportDataPointWithReporterDetails,
  AcceptedDataPointSource,
  QaReportDataPointVerdict,
} from '@clients/qaservice';

import { useApiClient } from '@/utils/useApiClient.ts';
import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import Tooltip from 'primevue/tooltip';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';

defineOptions({ name: 'DatasetReviewComparisonTable' });

const props = defineProps<{
  framework: DataTypeEnum;
  dataId: string;
  searchQuery: string;
  datasetReview: DatasetReviewResponse;
  dataMetaInformation: DataMetaInformation;
  hideEmptyFields: boolean;
}>();

const frameworkDefinition = computed(() => getFrontendFrameworkDefinition(props.framework));
const viewConfig = computed(() => frameworkDefinition.value?.getFrameworkViewConfiguration());
const mldtConfig = computed<MLDTConfig<FrameworkData> | undefined>(
  () => viewConfig.value?.configuration as MLDTConfig<FrameworkData> | undefined
);

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

/**
 * Build a flat list of table rows (sections and cell rows) from the MLDT configuration
 * and a single dataset. This function performs a recursive traversal of the
 * configuration tree.
 *
 * @param {MLDTConfig<FrameworkData>} config - MLDT view configuration for the framework.
 * @param {FrameworkData} data - The framework data instance to read values from.
 * @param {number} [level=0] - Current nesting level for section rows (used for styling).
 * @returns {KpiRow[]} The generated list of rows representing sections and KPI cells.
 */
function buildRowsFromConfig(config: MLDTConfig<FrameworkData>, data: FrameworkData, level = 0): KpiRow[] {
  const rows: KpiRow[] = [];
  for (const item of config) {
    if (item.type === 'section') {
      const section = item;
      const childRows = buildRowsFromConfig(section.children, data, level + 1);

      if (childRows.length > 0) {
        rows.push({ type: 'section', label: section.label, level: level }, ...childRows);
      }
    } else if (item.type === 'cell') {
      const cell = item;
      const cellRow: CellRow = {
        type: 'cell',
        label: cell.label,
        dataPointTypeId: cell.dataPointTypeId,
        originalDisplay: cell.valueGetter(data),
        explanation: cell.explanation,
      };

      // Apply the "Hide Empty Fields" logic
      if (!props.hideEmptyFields || !isRowEmpty(cellRow)) {
        rows.push(cellRow);
      }
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

// (5) Review Helpers

// --- Helpers to join review info ---
/**
 * Look up review information for a single data point type id from the
 * provided `datasetReview` prop.
 *
 * @param {string | undefined} dataPointTypeId - The data point type identifier to look up.
 * @returns {DataPointReviewDetails | undefined} The review info entry for the data point, or undefined when not found.
 */
function getReviewInfo(dataPointTypeId?: string): DataPointReviewDetails | undefined {
  if (!dataPointTypeId) return undefined;
  return props.datasetReview.dataPoints[dataPointTypeId];
}

/**
 * Returns the QA report for the given table row and reporter company ID.
 *
 * Looks up the datasetReview entry for the row's data point type and
 * returns the QaReportDataPointWithReporterDetails for the given reporter company if present.
 *
 * @param {CellRow} row - The table cell row describing the data point.
 * @param {string} reporterCompanyId - The reporter company identifier to match.
 * @returns {QaReportDataPointWithReporterDetails | undefined} The matching QA report summary or undefined when not found.
 */
function getQaReportFor(row: CellRow, reporterCompanyId: string): QaReportDataPointWithReporterDetails | undefined {
  if (!row.dataPointTypeId) return undefined;
  const dpEntry = props.datasetReview.dataPoints[row.dataPointTypeId];
  if (!dpEntry) return undefined;
  return dpEntry.qaReports.find((r) => r.reporterCompanyId === reporterCompanyId);
}

/**
 * Extracts the corrected display value from a QA report's JSON payload.
 *
 * The QA report stores correctedData as a JSON string. This helper parses
 * that string and returns the inner `value` property, or null when the
 * corrected value is not available or parsing fails.
 *
 * @param {QaReportDataPointWithReporterDetails | undefined} qaReport - QA report to extract value from.
 * @returns {string | null} The corrected display value or null when unavailable.
 */
function getCorrectedDisplayFromQaReport(qaReport: QaReportDataPointWithReporterDetails | undefined): string | null {
  if (!qaReport?.correctedData) return null;
  try {
    const parsed = JSON.parse(qaReport.correctedData);
    return parsed.value == null ? null : parsed.value;
  } catch {
    return null;
  }
}

/**
 * Determines whether the given source is the accepted source for the provided row.
 *
 * For QA sources the function additionally checks that the accepted QA report
 * originates from the provided reporter company id so the correct QA column can
 * be marked as accepted.
 *
 * @param {CellRow} row - The cell row to check.
 * @param {AcceptedDataPointSource} source - The source type to compare against.
 * @param {string} [reporterCompanyId] - Optional reporter company id (required for QA checks).
 * @returns {boolean} True when the provided source matches the accepted source for the row.
 */
function isAcceptedSource(row: CellRow, source: AcceptedDataPointSource, reporterCompanyId?: string): boolean {
  const reviewInfo = getReviewInfo(row.dataPointTypeId);
  if (reviewInfo?.acceptedSource !== source) return false;
  if (source !== AcceptedDataPointSource.Qa) return true;
  return (
    reporterCompanyId != null &&
    reviewInfo.companyIdOfAcceptedQaReport != null &&
    reviewInfo.companyIdOfAcceptedQaReport === reporterCompanyId
  );
}

/**
 * Determines whether a cell is considered empty for a specific source column.
 *
 * - Original: checks the computed original display object for an empty or missing value.
 * - Custom: checks the reviewInfo.customValue.
 * - Qa: checks the corrected QA value for the provided reporter company.
 *
 * @param {CellRow} cellRow - The cell row to inspect.
 * @param {AcceptedDataPointSource} source - The source type to test for emptiness.
 * @param {string} [reporterCompanyId] - Optional reporter company id for QA lookups.
 * @returns {boolean} True when the cell for the given source is empty or missing.
 */
function isCellEmpty(cellRow: CellRow, source: AcceptedDataPointSource, reporterCompanyId?: string): boolean {
  if (source === AcceptedDataPointSource.Original) {
    const original = cellRow.originalDisplay;
    return original == null || original.displayValue == null || original.displayValue === '';
  }
  if (source === AcceptedDataPointSource.Custom) {
    const reviewInfo = getReviewInfo(cellRow.dataPointTypeId);
    return reviewInfo?.customValue == null || reviewInfo.customValue === '';
  }
  if (source === AcceptedDataPointSource.Qa) {
    const report = reporterCompanyId == null ? undefined : getQaReportFor(cellRow, reporterCompanyId);
    const corrected = getCorrectedDisplayFromQaReport(report);
    return corrected == null || corrected === '';
  }
  return true;
}

/**
 * Decide whether the UI should render a rejected icon for a specific cell and source.
 *
 * The function returns false when there is no accepted source set, when the
 * cell is empty for the inspected source, or when the inspected source is the
 * accepted source. For QA sources the verdict on the QA report is also taken
 * into account (only non-accepted QA reports show as rejected).
 *
 * @param {CellRow} cellRow - The cell row to inspect.
 * @param {AcceptedDataPointSource} source - The source column being inspected.
 * @param {string} [reporterCompanyId] - Optional reporter company id for QA lookups.
 * @returns {boolean} True when a rejected icon should be shown.
 */
function shouldShowRejectedIcon(
  cellRow: CellRow,
  source: AcceptedDataPointSource,
  reporterCompanyId?: string
): boolean {
  const reviewInfo = getReviewInfo(cellRow.dataPointTypeId);
  if (reviewInfo?.acceptedSource == null) return false;
  if (isCellEmpty(cellRow, source, reporterCompanyId)) return false;
  if (isAcceptedSource(cellRow, source, reporterCompanyId)) return false;
  if (source === AcceptedDataPointSource.Qa) {
    const report = reporterCompanyId == null ? undefined : getQaReportFor(cellRow, reporterCompanyId);
    if (!report) return false;
    return report.verdict !== QaReportDataPointVerdict.QaAccepted;
  }
  return true;
}

/**
 * Returns true when a row has no visible values in any of the available columns
 * (original, all QA reporters, or custom). Used to honor the "hideEmptyFields"
 * prop when building the visible rows.
 *
 * @param {CellRow} cellRow - The row to test for emptiness.
 * @returns {boolean} True when the row is empty across all sources.
 */
function isRowEmpty(cellRow: CellRow): boolean {
  const isOriginalEmpty = isCellEmpty(cellRow, AcceptedDataPointSource.Original);
  const isCustomEmpty = isCellEmpty(cellRow, AcceptedDataPointSource.Custom);
  const isQaEmptyForAllCompanies = props.datasetReview.qaReporterCompanies.every((company) =>
    isCellEmpty(cellRow, AcceptedDataPointSource.Qa, company.reporterCompanyId)
  );

  return isOriginalEmpty && isCustomEmpty && isQaEmptyForAllCompanies;
}

/**
 * Opens judge modal
 *
 * @param {KpiRow} row - Input row of data point to open judge modal for.
 */
function openJudgeModal(row: KpiRow): void {
  if (row.type === 'section') return;
  console.log('open judge modal for', row.dataPointTypeId, row.label);
}

/**
 * Convert a string to Title Case (first letter capitalized for each word).
 *
 * @param {string} str - Input string to convert.
 * @returns {string} Title-cased string.
 */
const toTitleCase = (str: string): string => {
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

.cell-flex {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.accepted-check {
  margin-left: auto;
}

.rejected-check {
  margin-left: auto;
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

.p-datatable-wrapper {
  overflow-x: auto;
  overflow-y: visible;
}

.p-datatable-table th:first-child,
.p-datatable-table td:first-child {
  position: sticky;
  left: 0;
  z-index: 2;
}

/* slightly higher z-index for header so it stays above cells */
.p-datatable-table thead th:first-child {
  z-index: 3;
}
</style>
