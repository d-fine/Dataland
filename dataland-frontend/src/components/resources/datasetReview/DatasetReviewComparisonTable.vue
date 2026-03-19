<template>
  <div class="card p-0 overflow-hidden">
    <ShowMultipleReportsBanner
      data-test="multipleReportsBanner"
      v-if="showMultipleReportsBanner"
      :reporting-periods="sortedReportingPeriods"
      :reports="sortedReports"
    />
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
                v-for="qaReporter in datasetReview.qaReporters"
                :key="qaReporter.reporterUserId"
                class="horizontal-headers-size"
              >
                <div class="p-column-header-content">
                  <span class="p-column-title">
                    Corrected Datapoint
                    <span class="block text-xs font-normal">{{
                      qaReporter.reporterUserName || qaReporter.reporterEmailAddress || qaReporter.reporterUserId
                    }}</span>
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
                      'uppercase text-base font-medium': row.level === 0,
                      'text-base font-medium': row.level > 0,
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
                      <span class="table-left-label font-normal">
                        {{ row.label }}
                      </span>
                      <em
                        v-if="row.explanation"
                        class="material-icons inline-flex align-items-center ml-2 cursor-help"
                        aria-hidden="true"
                        v-tooltip.top="{ value: row.explanation }"
                      >
                        info
                      </em>
                    </div>
                  </td>

                  <!-- Original datapoint -->
                  <td class="vertical-align-top border-right-1 surface-border">
                    <div class="flex align-items-start gap-2">
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
                        class="pi pi-check text-green-500 ml-auto accepted-check"
                        aria-label="Accepted source"
                      ></span>
                      <span
                        v-else-if="shouldShowRejectedIcon(row, AcceptedDataPointSource.Original)"
                        class="pi pi-times text-red-500 ml-auto rejected-check"
                        aria-label="Rejected source"
                      ></span>
                    </div>
                  </td>

                  <!-- Corrected datapoint -->
                  <td
                    v-for="qaReporter in datasetReview.qaReporters"
                    :key="qaReporter.reporterUserId"
                    class="vertical-align-top border-right-1 surface-border"
                  >
                    <div class="flex align-items-start gap-2">
                      <span
                        v-if="
                          getQaReportFor(row, qaReporter.reporterUserId)?.verdict ===
                          QaReportDataPointVerdict.QaAccepted
                        "
                      >
                        QA Accepted
                      </span>
                      <span v-else-if="getQaReportFor(row, qaReporter.reporterUserId)" class="main-text-color">
                        {{ getCorrectedDisplayFromQaReport(getQaReportFor(row, qaReporter.reporterUserId)) ?? '—' }}
                      </span>
                      <span v-else class="main-text-color"> &ndash; </span>
                      <span
                        v-if="isAcceptedSource(row, AcceptedDataPointSource.Qa, qaReporter.reporterUserId)"
                        class="pi pi-check text-green-500 accepted-check"
                        aria-label="Accepted source"
                      ></span>
                      <span
                        v-else-if="shouldShowRejectedIcon(row, AcceptedDataPointSource.Qa, qaReporter.reporterUserId)"
                        class="pi pi-times text-red-500 rejected-check"
                        aria-label="Rejected source"
                      ></span>
                    </div>
                  </td>

                  <td class="vertical-align-top border-right-1 surface-border">
                    <div class="flex align-items-start gap-2">
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
import { computed } from 'vue';
import MultiLayerDataTableCell from '@/components/resources/dataTable/MultiLayerDataTableCell.vue';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import type { MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import type { AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import type {
  DataMetaInformation,
  EutaxonomyFinancialsData,
  EutaxonomyNonFinancialsData,
  NuclearAndGasData,
  SfdrData,
} from '@clients/backend';
import { DataTypeEnum } from '@clients/backend';
import {
  type DatasetJudgementResponse,
  type DataPointJudgement,
  type DataPointQaReport,
  AcceptedDataPointSource,
  QaReportDataPointVerdict,
} from '@clients/qaservice';

import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import Tooltip from 'primevue/tooltip';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { useGetFrameworkDataQuery } from '@/api-queries/backend/framework-data/useGetFrameworkDataQuery.ts';
import ShowMultipleReportsBanner from '@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue';
import { toTitleCase } from '@/utils/StringFormatter.ts';

defineOptions({ name: 'DatasetReviewComparisonTable' });

const props = defineProps<{
  framework: DataTypeEnum;
  dataId: string;
  searchQuery: string;
  datasetReview: DatasetJudgementResponse;
  dataMetaInformation: DataMetaInformation;
  hideEmptyFields: boolean;
}>();

const frameworkDefinition = computed(() => getFrontendFrameworkDefinition(props.framework));
const viewConfig = computed(() => frameworkDefinition.value?.getFrameworkViewConfiguration());
const mldtConfig = computed<MLDTConfig<FrameworkData> | undefined>(
  () => viewConfig.value?.configuration as MLDTConfig<FrameworkData> | undefined
);

const vTooltip = Tooltip;

const frameworkRef = computed(() => props.framework);
const dataIdRef = computed(() => props.dataId);

const showMultipleReportsBanner = computed(() => {
  const applicableFrameworks: DataTypeEnum[] = [
    DataTypeEnum.EutaxonomyNonFinancials,
    DataTypeEnum.EutaxonomyFinancials,
    DataTypeEnum.Sfdr,
    DataTypeEnum.NuclearAndGas,
  ];
  return applicableFrameworks.includes(props.framework);
});

const {
  data: originalDataAndMeta,
  isPending: loadingOriginal,
  error: errorOriginal,
} = useGetFrameworkDataQuery({
  framework: frameworkRef,
  dataId: dataIdRef,
});

const sortedReportingPeriods = computed(() => {
  const reportingPeriod = originalDataAndMeta.value?.reportingPeriod;
  return reportingPeriod ? [reportingPeriod] : [];
});

const sortedReports = computed(() => {
  const data = originalDataAndMeta.value?.data;
  if (!data) return [];
  switch (props.framework) {
    case DataTypeEnum.EutaxonomyNonFinancials: {
      const reports = (data as EutaxonomyNonFinancialsData).general?.referencedReports;
      return reports ? [reports] : [];
    }
    case DataTypeEnum.EutaxonomyFinancials: {
      const reports = (data as EutaxonomyFinancialsData).general?.general?.referencedReports;
      return reports ? [reports] : [];
    }
    case DataTypeEnum.Sfdr: {
      const reports = (data as SfdrData).general?.general?.referencedReports;
      return reports ? [reports] : [];
    }
    case DataTypeEnum.NuclearAndGas: {
      const reports = (data as NuclearAndGasData).general?.general?.referencedReports;
      return reports ? [reports] : [];
    }
    default: {
      return [];
    }
  }
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
 * @returns {DataPointJudgement | undefined} The review info entry for the data point, or undefined when not found.
 */
function getReviewInfo(dataPointTypeId?: string): DataPointJudgement | undefined {
  if (!dataPointTypeId) return undefined;
  return props.datasetReview.dataPoints[dataPointTypeId];
}

/**
 * Returns the QA report for the given table row and reporter user ID.
 *
 * Looks up the datasetReview entry for the row's data point type and
 * returns the DataPointQaReport for the given reporter if present.
 *
 * @param {CellRow} row - The table cell row describing the data point.
 * @param {string} reporterUserId - The userId of the user who uploaded the QA report.
 * @returns {DataPointQaReport | undefined} The matching QA report summary or undefined when not found.
 */
function getQaReportFor(row: CellRow, reporterUserId: string): DataPointQaReport | undefined {
  if (!row.dataPointTypeId) return undefined;
  const datapointEntry = props.datasetReview.dataPoints[row.dataPointTypeId];
  if (!datapointEntry) return undefined;
  return datapointEntry.qaReports.find((r) => r.reporterUserId === reporterUserId);
}

/**
 * Extracts the corrected display value from a QA report's JSON payload.
 *
 * The QA report stores correctedData as a JSON string. This helper parses
 * that string and returns the inner `value` property, or null when the
 * corrected value is not available or parsing fails.
 *
 * @param {DataPointQaReport | undefined} qaReport - QA report to extract value from.
 * @returns {string | null} The corrected display value or null when unavailable.
 */
function getCorrectedDisplayFromQaReport(qaReport: DataPointQaReport | undefined): string | null {
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
 * originates from the provided reporter user id so the correct QA column can
 * be marked as accepted.
 *
 * @param {CellRow} row - The cell row to check.
 * @param {AcceptedDataPointSource} source - The source type to compare against.
 * @param {string} [reporterUserId] - Optional reporter user id (required for QA checks).
 * @returns {boolean} True when the provided source matches the accepted source for the row.
 */
function isAcceptedSource(row: CellRow, source: AcceptedDataPointSource, reporterUserId?: string): boolean {
  const reviewInfo = getReviewInfo(row.dataPointTypeId);
  if (reviewInfo?.acceptedSource !== source) return false;
  if (source !== AcceptedDataPointSource.Qa) return true;
  return (
    reporterUserId != null &&
    reviewInfo.reporterUserIdOfAcceptedQaReport != null &&
    reviewInfo.reporterUserIdOfAcceptedQaReport === reporterUserId
  );
}

/**
 * Determines whether a cell is considered empty for a specific source column.
 *
 * - Original: checks the computed original display object for an empty or missing value.
 * - Custom: checks the reviewInfo.customValue.
 * - Qa: checks the corrected QA value for the provided reporter user id.
 *
 * @param {CellRow} cellRow - The cell row to inspect.
 * @param {AcceptedDataPointSource} source - The source type to test for emptiness.
 * @param {string} [reporterUserId] - Optional reporter user id for QA lookups.
 * @returns {boolean} True when the cell for the given source is empty or missing.
 */
function isCellEmpty(cellRow: CellRow, source: AcceptedDataPointSource, reporterUserId?: string): boolean {
  switch (source) {
    case AcceptedDataPointSource.Original: {
      const original = cellRow.originalDisplay;
      return original == null || original.displayValue == null || original.displayValue === '';
    }

    case AcceptedDataPointSource.Custom: {
      const reviewInfo = getReviewInfo(cellRow.dataPointTypeId);
      return reviewInfo?.customValue == null || reviewInfo.customValue === '';
    }

    case AcceptedDataPointSource.Qa: {
      const report = reporterUserId == null ? undefined : getQaReportFor(cellRow, reporterUserId);
      const corrected = getCorrectedDisplayFromQaReport(report);
      return corrected == null || corrected === '';
    }

    default:
      return true;
  }
}

/**
 * Decide whether the UI should render a rejected icon for a specific cell and source.
 *
 * The function returns false when there is no accepted source set, when the cell is empty for the inspected source,
 * or when the inspected source is the accepted source.
 * For QA sources the verdict on the QA report is also taken into account (only non-accepted QA reports show as rejected).
 *
 * @param {CellRow} cellRow - The cell row to inspect.
 * @param {AcceptedDataPointSource} source - The source column being inspected.
 * @param {string} [reporterUserId] - Optional reporter id for QA lookups.
 * @returns {boolean} True when a rejected icon should be shown.
 */
function shouldShowRejectedIcon(cellRow: CellRow, source: AcceptedDataPointSource, reporterUserId?: string): boolean {
  const reviewInfo = getReviewInfo(cellRow.dataPointTypeId);
  if (reviewInfo?.acceptedSource == null) return false;
  if (isCellEmpty(cellRow, source, reporterUserId)) return false;
  if (isAcceptedSource(cellRow, source, reporterUserId)) return false;
  if (source === AcceptedDataPointSource.Qa) {
    const report = reporterUserId == null ? undefined : getQaReportFor(cellRow, reporterUserId);
    if (!report) return false;
  }
  return true;
}

/**
 * Returns true when a row has no visible values in any of the available columns
 * (original, all QA reporters, or custom). Used to honor the "hideEmptyFields" prop when building the visible rows.
 *
 * @param {CellRow} cellRow - The row to test for emptiness.
 * @returns {boolean} True when the row is empty across all sources.
 */
function isRowEmpty(cellRow: CellRow): boolean {
  const isOriginalEmpty = isCellEmpty(cellRow, AcceptedDataPointSource.Original);
  const isCustomEmpty = isCellEmpty(cellRow, AcceptedDataPointSource.Custom);
  const isQaEmptyForAllColumns = props.datasetReview.qaReporters.every((qaReporter) =>
    isCellEmpty(cellRow, AcceptedDataPointSource.Qa, qaReporter.reporterUserId)
  );

  return isOriginalEmpty && isCustomEmpty && isQaEmptyForAllColumns;
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
</script>

<style scoped>
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
