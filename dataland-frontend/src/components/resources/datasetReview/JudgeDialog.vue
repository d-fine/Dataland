<template>
  <PrimeDialog
      id="judgeModal"
      :dismissable-mask="true"
      :modal="true"
      :pt="{ root: { style: { minWidth: '60rem', maxWidth: '60rem' } } }"
      v-model:visible="isOpen"
      @hide="emit('close')"
      data-test="judge-modal"
  >
    <!-- Header -->
    <template #header>
      <span class="p-dialog-title">{{ currentDataPointTypeId }}</span>
    </template>

    <!-- Loading / error states for dataset review -->
    <div v-if="isDatasetReviewLoading">
      Loading dataset review...
    </div>
    <div v-else-if="datasetReviewError">
      <Message severity="error">
        Failed to load dataset review.
      </Message>
    </div>

    <div v-else class="judge-modal__content">
      <!-- Top-left: Original datapoint -->
      <DatapointReadonlySection
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
          @accept="onAcceptClick('Original')"
          @show-popover="showPopover"
          @hide-popover="hidePopover"
      />

      <!-- Top-right: Corrected datapoint (QA reports) -->
      <DatapointReadonlySection
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
          @accept="onAcceptClick('Qa')"
          @prev="goToPreviousReport"
          @next="goToNextReport"
          @show-popover="showPopover"
          @hide-popover="hidePopover"
      />

      <!-- Separator line -->
      <div class="judge-modal__separator"></div>

      <!-- Bottom-left: Custom datapoint -->
      <CustomDatapointSection
          v-model:edit-mode-enabled="editModeEnabled"
          v-model:json="customJson"
          v-model:form-data="customFormData"
          :accept-disabled="isMutating"
          :can-copy-original="!!originalData"
          :can-copy-corrected="!!currentQaCorrectedData"
          @accept="onAcceptClick('Custom')"
          @copy-original="copyOriginalToCustom"
          @copy-corrected="copyCorrectedToCustom"
      />

      <!-- Bottom-right: Next datapoint selection & patch error -->
      <NextDatapointSection
          v-model:only-show-unreviewed="onlyShowUnreviewed"
          v-model:selected-next-data-point-type-id="selectedNextDataPointTypeId"
          :options="nextDataPointOptions"
          :patch-error="patchError"
          @go-to="goToSelectedDataPoint"
      />
    </div>

    <Popover
        ref="overflowPopover"
        placement="top"
        :pt="{ root: { style: { width: popoverWidth } } }"
    >
      <div class="judge-modal__overflow-popover-content">{{ popoverText }}</div>
    </Popover>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import type Keycloak from 'keycloak-js';
import PrimeDialog from 'primevue/dialog';
import Message from 'primevue/message';
import Popover from 'primevue/popover';

import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

import DatapointReadonlySection from '@/components/resources/datasetReview/DatapointReadonlySection.vue';
import CustomDatapointSection from '@/components/resources/datasetReview/CustomDatapointSection.vue';
import NextDatapointSection from '@/components/resources/datasetReview/NextDatapointSection.vue';
import type {
  CustomFormData,
  DataPointDetail,
  NextDatapointOption,
  QaReport,
  QaReporter,
} from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

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

// ===== API clients =====

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const datasetReviewControllerApi = apiClientProvider.apiClients.datasetReviewController;
const dataPointControllerApi = apiClientProvider.apiClients.dataPointController;

// ===== Dataset review =====

const mockDataPointsById: Record<string, DataPointDetail> = {
  'mock-dp-1': {
    value: '12345',
    quality: 'High',
    comment: 'Mock original datapoint for QA comparison. Test comment to check multiline display.',
    dataSource: {
      fileName: 'Sustainability_Report_2023.pdf',
      page: 12,
    },
  },
  'mock-dp-2': {
    value: '987',
    quality: 'Medium',
    comment: 'Mock original datapoint for custom acceptance.',
    dataSource: {
      fileName: 'MockSource-REF-77',
      page: '4-6',
    },
  },
  'mock-dp-3': {
    value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_GEOTHERMAL_2023_CONSOLIDATED_GROSS_NET_ADJUSTED',
    quality: 'Estimated_PreliminaryAudit_PendingFinalVerificationByExternalAuditorGmbH',
    comment: 'This value was extracted from the consolidated energy production appendix on pages 47 through 53 of the annual sustainability disclosure. The figure includes all renewable sources as defined under EU Taxonomy Article 10 and has been adjusted for grid losses according to the methodology described in footnote 23. Please cross-reference with the interim report published in Q2 before final acceptance.',
    dataSource: {
      fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
      page: '47-53',
    },
  },
};

function createMockDatasetReview() {
  return {
    dataPoints: {
      'kpi.energyConsumption': {
        dataPointId: 'mock-dp-1',
        acceptedSource: null,
        qaReports: [
          {
            qaReportId: 'mock-qa-1',
            verdict: 'QaPending',
            correctedData: JSON.stringify({
              value: 12000,
              quality: 'High',
              comment: 'Corrected based on updated table.',
              dataSource: { fileName: 'Sustainability_Report_2023.pdf', page: '13' },
            }),
            reporterUserId: 'mock-user-1',
          },
          {
            qaReportId: 'mock-qa-2',
            verdict: 'QaPending',
            correctedData: JSON.stringify({
              value: 11890,
              quality: 'High',
              comment: 'Adjusted for unit conversion.',
              dataSource: { fileName: 'Sustainability_Report_2023.pdf', page: '14-15' },
            }),
            reporterUserId: 'mock-user-2',
          },
        ],
      },
      'kpi.co2Emissions': {
        dataPointId: 'mock-dp-2',
        acceptedSource: null,
        qaReports: [],
      },
      'kpi.energyProduction': {
        dataPointId: 'mock-dp-3',
        acceptedSource: null,
        qaReports: [
          {
            qaReportId: 'mock-qa-3',
            verdict: 'QaPending',
            correctedData: JSON.stringify({
              value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_GEOTHERMAL_2023_CONSOLIDATED_GROSS_NET_ADJUSTED_REVISED',
              quality: 'Verified_FinalAudit_ConfirmedByExternalAuditorGmbH_SignedOff',
              comment: 'Corrected after cross-referencing the interim Q2 report and the footnote methodology in section 5.3. The original value underreported geothermal contribution by approximately 3.7% due to a unit conversion error. This revision aligns the figure with the EU Taxonomy gross production definition and has been signed off by the external auditor. No further changes expected.',
              dataSource: { fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf', page: '47-53' },
            }),
            reporterUserId: 'mock-user-1',
          },
          {
            qaReportId: 'mock-qa-4',
            verdict: 'QaPending',
            correctedData: JSON.stringify({
              value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_2023_NET_ADJUSTED_EXCL_GEOTHERMAL',
              quality: 'Estimated_SecondReview_PendingGeothermalReclassification',
              comment: 'Alternative correction excluding geothermal pending reclassification under the updated EU Taxonomy delegated act. Reviewer recommends holding acceptance until the reclassification outcome is published in the official journal. See internal ticket DL-4892 for tracking.',
              dataSource: { fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf', page: '51-52' },
            }),
            reporterUserId: 'mock-user-2',
          },
        ],
      },
    },
    qaReporters: [
      { reporterUserId: 'mock-user-1', reporterUserName: 'Jane QA', reporterEmailAddress: 'jane.qa@example.com' },
      { reporterUserId: 'mock-user-2', reporterUserName: 'Alex QA', reporterEmailAddress: 'alex.qa@example.com' },
    ],
  } as any;
}

const datasetReview = ref(createMockDatasetReview());
const isDatasetReviewLoading = ref(false);
const datasetReviewError = ref(null);

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
  if (!datasetReview.value?.dataPoints) return null;
  return datasetReview.value.dataPoints[currentDataPointTypeId.value] ?? null;
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
  return (meta.qaReports as QaReport[]).filter((r) => r.verdict !== 'QaAccepted');
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
  if (!datasetReview.value?.qaReporters) return map;
  for (const r of datasetReview.value.qaReporters as QaReporter[]) {
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
      document: String(originalData.value.dataSource?.document ?? ''),
      pages: String(originalData.value.dataSource?.pages ?? ''),
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
      pages: String(currentQaCorrectedData.value.dataSource?.pages ?? ''),
      comment: String(currentQaCorrectedData.value.comment ?? ''),
    };
  }
}

// ===== Next datapoint =====

const onlyShowUnreviewed = ref<boolean>(true);
const selectedNextDataPointTypeId = ref<string | null>(null);

const nextDataPointOptions = computed<NextDatapointOption[]>(() => {
  if (!datasetReview.value?.dataPoints) return [];
  const options: NextDatapointOption[] = [];

  const entries = Object.entries(datasetReview.value.dataPoints) as [string, any][];

  for (const [dataPointType, meta] of entries) {
    const reviewed = meta.acceptedSource !== null;
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
  if (!datasetReview.value?.dataPoints) return null;
  const allEntries = Object.entries(datasetReview.value.dataPoints) as [string, any][];
  const startIndex = allEntries.findIndex(([key]) => key === afterId);
  const rotated = [...allEntries.slice(startIndex + 1), ...allEntries.slice(0, startIndex)];
  const [key] = rotated.find(([, meta]) => meta.acceptedSource === null) ?? [];
  return key ?? null;
}

function goToSelectedDataPoint(): void {
  if (!selectedNextDataPointTypeId.value) return;
  const currentSelection = selectedNextDataPointTypeId.value;
  currentDataPointTypeId.value = currentSelection;
  selectedNextDataPointTypeId.value = findNextUnreviewedAfter(currentSelection);
  resetStateForCurrentDataPoint();
}

function markCurrentAsReviewed(source: string): void {
  if (datasetReview.value?.dataPoints?.[currentDataPointTypeId.value]) {
    datasetReview.value.dataPoints[currentDataPointTypeId.value].acceptedSource = source;
  }
}

function onAcceptClick(source: 'Original' | 'Qa' | 'Custom'): void {
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
  const width = formInput
    ? formInput.getBoundingClientRect().width
    : anchor.getBoundingClientRect().width;
  popoverWidth.value = `${width}px`;

  popoverText.value = text;
  overflowPopover.value?.show(event, anchor);
}

function hidePopover(): void {
  overflowPopover.value?.hide();
}
</script>

<style scoped lang="scss">
.judge-modal__content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto 20rem;
  gap: var(--spacing-xs);
}

.judge-modal__separator {
  grid-column: 1 / -1;
  height: 2px;
  background-color: var(--p-content-border-color);
  margin: var(--spacing-xs) 0;
}

.judge-modal__overflow-popover-content {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
