<template>
  <PrimeDialog
    id="judgeModal"
    :dismissable-mask="true"
    :modal="true"
    :pt="{ root: { style: { width: '80vw', maxHeight: '80vh' } } }"
    v-model:visible="isOpen"
    @hide="emit('close')"
    data-test="judge-modal"
  >
    <!-- Header -->
    <template #header>
      <div class="judge-modal__header">
        <span class="p-dialog-title">{{ currentDataPointTypeId }}</span>
        <span
          v-if="verdictBadge"
          class="judge-modal__verdict-badge"
          :class="verdictBadge.cssClass"
          data-test="verdict-badge"
        >
          {{ verdictBadge.label }}
        </span>
      </div>
    </template>

    <!-- Loading / error states for dataset review -->
    <div v-if="isDatasetJudgementPending">Loading dataset review...</div>
    <div v-else-if="datasetReviewError">
      <Message severity="error"> Failed to load dataset review. </Message>
    </div>

    <div v-else class="judge-modal__content">
      <!-- Top-left: Original datapoint -->
      <JudgeDialogTopSection
        title="Original datapoint"
        :data="originalData"
        :is-loading="isOriginalLoading"
        :load-error="originalError"
        empty-text="No original datapoint data available."
        accept-label="ACCEPT ORIGINAL"
        :accept-disabled="isMutating"
        accept-data-test="accept-original-button"
        data-test="original-datapoint-section"
        :show-nav="false"
        :nav-index="0"
        @accept="onAcceptClick(AcceptedDataPointSource.Original)"
        @show-popover="showPopover"
        @hide-popover="hidePopover"
      />

      <!-- Top-right: Corrected datapoint (QA reports) -->
      <JudgeDialogTopSection
        title="Corrected datapoint"
        :data="currentQaCorrectedData"
        empty-text="No QA reports available."
        accept-label="ACCEPT REPORT"
        :accept-disabled="isMutating || filteredQaReports.length === 0 || !currentQaReport"
        accept-data-test="accept-report-button"
        data-test="corrected-datapoint-section"
        :show-nav="filteredQaReports.length > 0"
        :nav-index="currentQaReportIndex"
        :nav-count="filteredQaReports.length"
        :nav-label="currentQaReporterLabel"
        @accept="onAcceptClick(AcceptedDataPointSource.Qa)"
        @prev="goToPreviousReport"
        @next="goToNextReport"
        @show-popover="showPopover"
        @hide-popover="hidePopover"
      />

      <!-- Bottom-left: Custom datapoint -->
      <JudgeDialogCustomSection
        v-model:edit-mode-enabled="editModeEnabled"
        v-model:json="customJson"
        v-model:form-data="customFormData"
        :accept-disabled="isMutating"
        :can-copy-original="!!originalData"
        :can-copy-corrected="!!currentQaCorrectedData"
        :available-documents="availableDocuments"
        @accept="onAcceptClick(AcceptedDataPointSource.Custom)"
        @copy-original="copyOriginalToCustom"
        @copy-corrected="copyCorrectedToCustom"
      />

      <!-- Bottom-right: Next datapoint selection & patch error -->
      <JudgeDialogNextSection
        v-model:only-show-unreviewed="onlyShowUnreviewed"
        v-model:selected-next-data-point-type-id="selectedNextDataPointTypeId"
        :options="nextDataPointOptions"
        :patch-error="patchError"
        @go-to="goToSelectedDataPoint"
      />
    </div>

    <Popover ref="overflowPopover" placement="top" :pt="{ root: { style: { width: popoverWidth } } }">
      <div class="judge-modal__overflow-popover-content">{{ popoverText }}</div>
    </Popover>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import PrimeDialog from 'primevue/dialog';
import Message from 'primevue/message';
import Popover from 'primevue/popover';

import JudgeDialogTopSection from '@/components/resources/datasetReview/JudgeDialogTopSection.vue';
import JudgeDialogCustomSection from '@/components/resources/datasetReview/JudgeDialogCustomSection.vue';
import JudgeDialogNextSection from '@/components/resources/datasetReview/JudgeDialogNextSection.vue';
import type {
  CustomFormData,
  DataPointDetail,
  DocumentOption,
  NextDatapointOption,
  QaReport,
  QaReporter,
} from '@/components/resources/datasetReview/JudgeDialogTypes.ts';
import { useDatasetReviewQuery } from '@/api-queries/qa-service/dataset-judgement/useDatasetReviewQuery.ts';
import { AcceptedDataPointSource } from '@clients/qaservice';

// ===== Props & emits =====
const DEFAULT_CUSTOM_JSON = JSON.stringify(
  { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
  null,
  2
);

const DEFAULT_CUSTOM_FORM_DATA: CustomFormData = {
  value: '',
  quality: '',
  document: '',
  pages: '',
  comment: '',
};

const props = defineProps<{
  datasetReviewId: string;
  dataPointTypeId: string;
}>();

const emit = defineEmits<{
  close: [];
}>();

// v-model:visible from parent
const isOpen = defineModel<boolean>('isOpen');

// ===== Dataset review =====

const mockDataPointsById: Record<string, DataPointDetail> = {
  'kpi.energyConsumption': {
    value: '12345',
    quality: 'Reported',
    comment: 'Mock original datapoint for QA comparison. Test comment to check multiline display.',
    dataSource: {
      page: '1026',
      tagName: 'web services',
      fileName: 'Sustainability_Report_2023.pdf',
      fileReference: '1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868',
      publicationDate: '2024-01-07',
    },
  },
  'kpi.co2Emissions': {
    value: '987',
    quality: 'Incomplete',
    comment: 'Mock original datapoint for custom acceptance.',
    dataSource: {
      fileName: 'MockSource-REF-77',
      fileReference: 'abcklwe78324',
      page: '4-6',
    },
  },
  'kpi.energyProduction': {
    value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_GEOTHERMAL_2023_CONSOLIDATED_GROSS_NET_ADJUSTED',
    quality: 'Estimated',
    comment:
      'This value was extracted from the consolidated energy production appendix on pages 47 through 53 of the annual sustainability disclosure. The figure includes all renewable sources as defined under EU Taxonomy Article 10 and has been adjusted for grid losses according to the methodology described in footnote 23. Please cross-reference with the interim report published in Q2 before final acceptance.',
    dataSource: {
      fileName: 'Annual_Sustainability_Disclosure.pdf',
      page: '47-53',
      publicationDate: '2023-01-08',
    },
  },
  'mock-dp-4': {
    quality: 'NoDataFound',
  },
};

function createMockDatasetReview() {
  return {
    dataPoints: {
      'kpi.energyConsumption': {
        dataPointId: 'kpi.energyConsumption',
        acceptedSource: null,
        qaReports: [
          {
            qaReportId: 'mock-qa-1',
            verdict: 'QaAccepted',
            correctedData: JSON.stringify({
              value: 12000,
              quality: 'Reported',
              comment: 'Corrected based on updated table.',
              dataSource: { fileName: 'Sustainability_Report_2023.pdf', page: '13' },
            }),
            reporterUserId: 'mock-user-1',
          },
          {
            qaReportId: 'mock-qa-2',
            verdict: 'QaAccepted',
            correctedData: JSON.stringify({
              value: 11890,
              quality: 'Reported',
              comment: 'Adjusted for unit conversion.',
              dataSource: { fileName: 'Sustainability_Report_2023.pdf', page: '14-15' },
            }),
            reporterUserId: 'mock-user-2',
          },
        ],
      },
      'kpi.co2Emissions': {
        dataPointId: 'kpi.co2Emissions',
        acceptedSource: null,
        qaReports: [],
      },
      'kpi.energyProduction': {
        dataPointId: 'kpi.energyProduction',
        acceptedSource: null,
        qaReports: [
          {
            qaReportId: 'mock-qa-3',
            verdict: 'QaAccepted',
            correctedData: JSON.stringify({
              value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_GEOTHERMAL_2023_CONSOLIDATED_GROSS_NET_ADJUSTED_REVISED',
              quality: 'Incomplete',
              comment:
                'Corrected after cross-referencing the interim Q2 report and the footnote methodology in section 5.3. The original value underreported geothermal contribution by approximately 3.7% due to a unit conversion error. This revision aligns the figure with the EU Taxonomy gross production definition and has been signed off by the external auditor. No further changes expected.',
              dataSource: {
                fileName:
                  'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
                page: '47-53',
              },
            }),
            reporterUserId: 'mock-user-1',
          },
          {
            qaReportId: 'mock-qa-4',
            verdict: 'QaRejected',
            correctedData: JSON.stringify({
              value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_2023_NET_ADJUSTED_EXCL_GEOTHERMAL',
              quality: 'NoDataFound',
              comment:
                'Alternative correction excluding geothermal pending reclassification under the updated EU Taxonomy delegated act. Reviewer recommends holding acceptance until the reclassification outcome is published in the official journal. See internal ticket DL-4892 for tracking.',
              dataSource: {
                fileName:
                  'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
                page: '51-52',
              },
            }),
            reporterUserId: 'mock-user-2',
          },
          {
            qaReportId: 'mock-qa-5',
            verdict: 'QaAccepted',
            correctedData: JSON.stringify({
              value: 'No',
              quality: 'Incomplete',
              comment: 'program neural circuit',
              dataSource: {
                page: '1026',
                tagName: 'web services',
                fileName: 'Sustainability_Report_2023.pdf',
                fileReference: '1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868',
                publicationDate: '2024-01-07',
              },
            }),
            reporterUserId: 'mock-user-3',
          },
        ],
      },
      'mock-dp-4': {
        dataPointId: 'mock-dp-4',
        acceptedSource: null,
        qaReports: [],
      },
    },
    qaReporters: [
      { reporterUserId: 'mock-user-1', reporterUserName: 'Jane QA', reporterEmailAddress: 'jane.qa@example.com' },
      { reporterUserId: 'mock-user-2', reporterUserName: 'Alex QA', reporterEmailAddress: 'alex.qa@example.com' },
      { reporterUserId: 'mock-user-3', reporterUserName: 'Peter QA', reporterEmailAddress: 'peter.qa@example.com' },
    ],
  } as any;
}

const datasetJudgementId = computed(() => props.datasetReviewId);
const {
  data: datasetJudgement,
  isPending: isDatasetJudgementPending,
  isError: datasetReviewError,
} = useDatasetReviewQuery({ datasetJudgementId: datasetJudgementId });

const isMutating = ref(false);
const patchError = ref<string | null>(null);

// ===== Current datapoint selection =====

const currentDataPointTypeId = ref<string>(props.dataPointTypeId);

watch(
  () => props.dataPointTypeId,
  (newVal) => {
    currentDataPointTypeId.value = newVal;
    resetStateForCurrentDataPoint();
  }
);

const currentDataPointMeta = computed<any | null>(() => {
  if (!datasetJudgement.value?.dataPoints) return null;
  return datasetJudgement.value.dataPoints[currentDataPointTypeId.value] ?? null;
});

// ===== Original datapoint =====

const originalData = ref<DataPointDetail | null>(null);
const isOriginalLoading = ref<boolean>(false);
const originalError = ref<unknown | null>(null);

async function loadOriginalDataPoint(): Promise<void> {
  originalData.value = null;
  originalError.value = null;

  const meta = currentDataPointMeta.value;
  if (!meta || !meta.dataPointId) return;

  isOriginalLoading.value = true;
  try {
    originalData.value = mockDataPointsById[meta.dataPointId] ?? null;
  } catch (error) {
    console.error('Failed to load original datapoint', error);
    originalError.value = error;
  } finally {
    isOriginalLoading.value = false;
  }
}

watch(
  () => currentDataPointTypeId.value,
  () => {
    loadOriginalDataPoint().catch((error) => console.error(error));
  },
  { immediate: true }
);

// ===== QA reports =====

const filteredQaReports = computed<QaReport[]>(() => {
  const meta = currentDataPointMeta.value;
  if (!meta?.qaReports) return [];
  return meta.qaReports as QaReport[];
});

const verdictBadge = computed<{ label: string; cssClass: string } | null>(() => {
  const meta = currentDataPointMeta.value;
  if (!meta) return null;
  const allReports = (meta.qaReports as QaReport[]) ?? [];
  if (allReports.length === 0) return { label: 'QA NOT ATTEMPTED', cssClass: 'judge-modal__verdict-badge--yellow' };
  if (allReports.every((r) => r.verdict === 'QaAccepted'))
    return { label: 'QA ACCEPTED', cssClass: 'judge-modal__verdict-badge--green' };
  if (allReports.some((r) => r.verdict === 'QaRejected'))
    return { label: 'QA REJECTED', cssClass: 'judge-modal__verdict-badge--red' };
  return { label: 'QA INCONCLUSIVE', cssClass: 'judge-modal__verdict-badge--yellow' };
});

const currentQaReportIndex = ref<number>(0);

watch(
  () => filteredQaReports.value.length,
  () => {
    currentQaReportIndex.value = 0;
  }
);

const currentQaReport = computed<QaReport | null>(() => {
  const list = filteredQaReports.value;
  if (!list.length) return null;
  return list[currentQaReportIndex.value] ?? list[0];
});

const qaReportersById = computed<Record<string, QaReporter>>(() => {
  const map: Record<string, QaReporter> = {};
  if (!datasetJudgement.value?.qaReporters) return map;
  for (const r of datasetJudgement.value.qaReporters as QaReporter[]) {
    map[r.reporterUserId] = r;
  }
  return map;
});

const currentQaReporterLabel = computed(() => {
  const report = currentQaReport.value;
  if (!report) return 'No QA report selected';
  const reporter = qaReportersById.value[report.reporterUserId];
  if (reporter?.reporterUserName) return reporter.reporterUserName;
  if (reporter?.reporterEmailAddress) return reporter.reporterEmailAddress;
  return report.reporterUserId;
});

const currentQaCorrectedData = computed<DataPointDetail | null>(() => {
  if (!currentQaReport.value?.correctedData) return null;
  try {
    return JSON.parse(currentQaReport.value.correctedData) as DataPointDetail;
  } catch (error) {
    console.error('Failed to parse correctedData JSON', error);
    return null;
  }
});

function goToPreviousReport(): void {
  if (currentQaReportIndex.value > 0) {
    currentQaReportIndex.value -= 1;
  }
}

function goToNextReport(): void {
  if (currentQaReportIndex.value < filteredQaReports.value.length - 1) {
    currentQaReportIndex.value += 1;
  }
}

// ===== Custom datapoint =====

// Derive available documents from the mock data points.
// Replace this with a real API call analogous to ExtendedDataPointFormFieldDialog:
//   const response = await documentControllerApi.searchForDocumentMetaInformation(companyId);
//   availableDocuments.value = response.data
//     .filter((doc) => doc.documentName && doc.documentId)
//     .map((doc) => ({
//       label: doc.documentName!,
//       value: doc.documentName!,
//       dataSource: {
//         fileName: doc.documentName ?? null,
//         fileReference: doc.documentId ?? null,
//         publicationDate: doc.publicationDate ?? null,
//       },
//     }));
const availableDocuments = computed<DocumentOption[]>(() => {
  const seen = new Set<string>();
  const options: DocumentOption[] = [];
  for (const dataPoint of Object.values(mockDataPointsById)) {
    const { dataSource } = dataPoint;
    const fileName = dataSource?.fileName;
    if (!fileName || seen.has(String(fileName))) continue;
    seen.add(String(fileName));
    options.push({
      label: String(fileName),
      value: String(fileName),
      dataSource: { ...dataSource },
    });
  }
  return options;
});

const editModeEnabled = ref<boolean>(false);
const customJson = ref<string>(DEFAULT_CUSTOM_JSON);
const customFormData = ref<CustomFormData>({ ...DEFAULT_CUSTOM_FORM_DATA });

function copyOriginalToCustom(): void {
  if (!originalData.value) return;

  if (editModeEnabled.value) {
    customJson.value = JSON.stringify(originalData.value, null, 2);
  } else {
    customFormData.value = {
      value: String(originalData.value.value ?? ''),
      quality: String(originalData.value.quality ?? ''),
      document: String(originalData.value.dataSource?.fileName ?? ''),
      pages: String(originalData.value.dataSource?.page ?? ''),
      comment: String(originalData.value.comment ?? ''),
    };
  }
}

function copyCorrectedToCustom(): void {
  if (!currentQaCorrectedData.value) return;

  if (editModeEnabled.value) {
    customJson.value = JSON.stringify(currentQaCorrectedData.value, null, 2);
  } else {
    customFormData.value = {
      value: String(currentQaCorrectedData.value.value ?? ''),
      quality: String(currentQaCorrectedData.value.quality ?? ''),
      document: String(currentQaCorrectedData.value.dataSource?.fileName ?? ''),
      pages: String(currentQaCorrectedData.value.dataSource?.page ?? ''),
      comment: String(currentQaCorrectedData.value.comment ?? ''),
    };
  }
}

// ===== Next datapoint =====

const onlyShowUnreviewed = ref<boolean>(true);
const selectedNextDataPointTypeId = ref<string | null>(null);

function isDataPointJudged(meta: any): boolean {
  if (meta.acceptedSource !== null) return true;
  const reports = (meta.qaReports as QaReport[]) ?? [];
  return reports.length > 0 && reports.every((r) => r.verdict === 'QaAccepted');
}

const nextDataPointOptions = computed<NextDatapointOption[]>(() => {
  if (!datasetJudgement.value?.dataPoints) return [];
  const options: NextDatapointOption[] = [];

  const entries = Object.entries(datasetJudgement.value.dataPoints) as [string, any][];

  for (const [dataPointType, meta] of entries) {
    const reviewed = isDataPointJudged(meta);
    if (onlyShowUnreviewed.value && reviewed) continue;

    options.push({
      label: dataPointType,
      value: dataPointType,
      reviewed,
    });
  }
  return options;
});

function findNextUnreviewedAfter(afterId: string): string | null {
  if (!datasetJudgement.value?.dataPoints) return null;
  const allEntries = Object.entries(datasetJudgement.value.dataPoints) as [string, any][];
  const startIndex = allEntries.findIndex(([key]) => key === afterId);
  const rotated = [...allEntries.slice(startIndex + 1), ...allEntries.slice(0, startIndex)];
  const [key] = rotated.find(([, meta]) => !isDataPointJudged(meta)) ?? [];
  return key ?? null;
}

function goToSelectedDataPoint(): void {
  if (!selectedNextDataPointTypeId.value) return;
  const currentSelection = selectedNextDataPointTypeId.value;
  currentDataPointTypeId.value = currentSelection;
  selectedNextDataPointTypeId.value = findNextUnreviewedAfter(currentSelection);
  resetStateForCurrentDataPoint();
}

function markCurrentAsReviewed(source: AcceptedDataPointSource): void {
  if (datasetJudgement.value?.dataPoints?.[currentDataPointTypeId.value]) {
    datasetJudgement.value.dataPoints[currentDataPointTypeId.value].acceptedSource = source;
  }
}

function onAcceptClick(source: AcceptedDataPointSource): void {
  isMutating.value = true;
  patchError.value = null;
  setTimeout(() => {
    const next = findNextUnreviewedAfter(currentDataPointTypeId.value);
    markCurrentAsReviewed(source);
    isMutating.value = false;
    selectedNextDataPointTypeId.value = next;
    if (next) goToSelectedDataPoint();
  }, 1000);
}

function resetStateForCurrentDataPoint(): void {
  patchError.value = null;
  currentQaReportIndex.value = 0;
  customJson.value = DEFAULT_CUSTOM_JSON;
  customFormData.value = { ...DEFAULT_CUSTOM_FORM_DATA };
}

// ===== Overflow popover =====

const overflowPopover = ref<InstanceType<typeof Popover> | null>(null);
const popoverText = ref<string>('');
const popoverWidth = ref<string>('auto');

function showPopover(event: MouseEvent, text: string): void {
  const btn = event.currentTarget as HTMLElement;
  const td = btn.closest('td') as HTMLElement | null;
  const anchor = td ?? btn;

  const dialog = btn.closest('.p-dialog') ?? btn.closest('#judgeModal') ?? document;
  const formInput = (dialog as Element).querySelector('.judge-modal__form-field') as HTMLElement | null;
  const width = formInput ? formInput.getBoundingClientRect().width : anchor.getBoundingClientRect().width;
  popoverWidth.value = `${width}px`;

  popoverText.value = text;
  overflowPopover.value?.show(event, anchor);
}

function hidePopover(): void {
  overflowPopover.value?.hide();
}
</script>

<style scoped lang="scss">
.p-dialog-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
}
.judge-modal__header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  width: 100%;
  flex: 1;
}

.judge-modal__verdict-badge {
  font-size: var(--font-size-sm);
  white-space: nowrap;

  &--green {
    background-color: var(--p-green-100);
    color: var(--p-green-700);
  }

  &--red {
    background-color: var(--p-red-100);
    color: var(--p-red-700);
  }

  &--yellow {
    background-color: var(--p-yellow-100);
    color: var(--p-yellow-700);
  }
}

.judge-modal__content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto 1fr;
  gap: var(--spacing-lg);
  flex: 1;
  min-height: 0;
}

.judge-modal__separator {
  grid-column: 1 / -1;
  height: 2px;
  background-color: var(--p-content-border-color);
  margin: var(--spacing-xxs) 0;
}

.judge-modal__overflow-popover-content {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
