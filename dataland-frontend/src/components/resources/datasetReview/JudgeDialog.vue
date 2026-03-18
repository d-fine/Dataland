<template>
  <PrimeDialog
      id="judgeModal"
      :dismissable-mask="true"
      :modal="true"
      class="col-8"
      style="min-width: 60rem; max-width: 90rem"
      v-model:visible="isOpen"
      @hide="handleClose"
      data-test="judge-modal"
  >
    <!-- Header -->
    <template #header>
      <div class="judge-modal__header">
        <span class="p-dialog-title">
          <span class="judge-modal__kpi-name">{{ currentDataPointTypeId }}</span>
        </span>
      </div>
    </template>

    <!-- Loading / error states for dataset review -->
    <div v-if="isDatasetReviewLoading" class="judge-modal__loading">
      Loading dataset review...
    </div>
    <div v-else-if="datasetReviewError" class="judge-modal__error">
      <Message severity="error">
        Failed to load dataset review.
      </Message>
    </div>

    <div v-else class="judge-modal__content">
      <!-- Top-left: Original datapoint -->
      <div class="judge-modal__grid-cell judge-modal__grid-top-left">
        <section class="judge-modal__section" data-test="original-datapoint-section">
          <h3 class="judge-modal__section-title">Original datapoint</h3>

          <div v-if="isOriginalLoading" class="judge-modal__subloading">
            Loading original datapoint...
          </div>
          <div v-else-if="originalError" class="judge-modal__suberror">
            <Message severity="error">
              Failed to load original datapoint.
            </Message>
          </div>
          <div v-else-if="originalData">
            <table class="judge-modal__datatable" aria-label="Original datapoint">
              <tbody>
              <tr>
                <th>Value</th>
                <td>{{ originalData.value ?? '—' }}</td>
              </tr>
              <tr>
                <th>Quality</th>
                <td>{{ originalData.quality ?? '—' }}</td>
              </tr>
              <tr>
                <th>Document</th>
                <td>
                    <span class="nowrap">
                      {{ originalData.dataSource?.fileName ?? originalData.dataSource?.fileReference ?? '—' }}
                    </span>
                </td>
              </tr>
              <tr>
                <th>Page / Range</th>
                <td>
                    <span v-if="originalData.dataSource?.pageRange">
                      {{ originalData.dataSource.pageRange }}
                    </span>
                  <span v-else-if="originalData.dataSource?.page">
                      {{ originalData.dataSource.page }}
                    </span>
                  <span v-else>—</span>
                </td>
              </tr>
              <tr>
                <th>Comment</th>
                <td class="judge-modal__multiline">
                  {{ originalData.comment ?? '—' }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
          <div v-else>
            <span>No original datapoint data available.</span>
          </div>

          <div class="judge-modal__section-actions">
            <PrimeButton
                label="ACCEPT ORIGINAL"
                @click=""
                :disabled="isMutating"
                data-test="accept-original-button"
            />
          </div>
        </section>
      </div>

      <!-- Top-right: Corrected datapoint (QA reports) -->
      <div class="judge-modal__grid-cell judge-modal__grid-top-right">
        <section class="judge-modal__section" data-test="corrected-datapoint-section">
          <div class="judge-modal__section-header-with-nav">
            <h3 class="judge-modal__section-title">
              Corrected datapoint
              <span v-if="filteredQaReports.length > 0" class="judge-modal__qa-counter">
                ({{ currentQaReportIndex + 1 }} / {{ filteredQaReports.length }})
              </span>
            </h3>

            <div v-if="filteredQaReports.length > 0" class="judge-modal__qa-nav">
              <PrimeButton
                  icon="pi pi-chevron-left"
                  variant="text"
                  @click="goToPreviousReport"
                  :disabled="currentQaReportIndex === 0"
                  data-test="qa-prev-button"
              />
              <div class="judge-modal__qa-reporter">
                {{ currentQaReporterLabel }}
              </div>
              <PrimeButton
                  icon="pi pi-chevron-right"
                  variant="text"
                  @click="goToNextReport"
                  :disabled="currentQaReportIndex === filteredQaReports.length - 1"
                  data-test="qa-next-button"
              />
            </div>
          </div>

          <div v-if="filteredQaReports.length === 0" class="judge-modal__no-qa">
            No QA reports available.
          </div>

          <div v-else>

            <div v-if="currentQaCorrectedData">
              <table class="judge-modal__datatable" aria-label="Corrected datapoint">
                <tbody>
                <tr>
                  <th>Value</th>
                  <td>{{ currentQaCorrectedData.value ?? '—' }}</td>
                </tr>
                <tr>
                  <th>Quality</th>
                  <td>{{ currentQaCorrectedData.quality ?? '—' }}</td>
                </tr>
                <tr>
                  <th>Document</th>
                  <td>
                      <span class="nowrap">
                        {{
                          currentQaCorrectedData.dataSource?.fileName ??
                          currentQaCorrectedData.dataSource?.fileReference ??
                          '—'
                        }}
                      </span>
                  </td>
                </tr>
                <tr>
                  <th>Page / Range</th>
                  <td>
                      <span v-if="currentQaCorrectedData.dataSource?.pageRange">
                        {{ currentQaCorrectedData.dataSource.pageRange }}
                      </span>
                    <span v-else-if="currentQaCorrectedData.dataSource?.page">
                        {{ currentQaCorrectedData.dataSource.page }}
                      </span>
                    <span v-else>—</span>
                  </td>
                </tr>
                <tr>
                  <th>Comment</th>
                  <td class="judge-modal__multiline">
                    {{ currentQaCorrectedData.comment ?? '—' }}
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="judge-modal__section-actions">
            <PrimeButton
                label="ACCEPT REPORT"
                @click=""
                :disabled="isMutating || filteredQaReports.length === 0 || !currentQaReport"
                data-test="accept-report-button"
            />
          </div>
        </section>
      </div>

      <!-- Bottom-left: Custom datapoint -->
      <div class="judge-modal__grid-cell judge-modal__grid-bottom-left">
        <section class="judge-modal__section" data-test="custom-datapoint-section">
          <div class="judge-modal__section-header-row">
            <h3 class="judge-modal__section-title">Custom datapoint</h3>
          </div>

          <div class="judge-modal__custom-actions">
            <PrimeButton
                label="Copy original datapoint"
                variant="outlined"
                @click="copyOriginalToCustom"
                :disabled="!originalData"
                data-test="copy-original-to-custom"
            />
            <PrimeButton
                label="Copy corrected datapoint"
                variant="outlined"
                @click="copyCorrectedToCustom"
                :disabled="!currentQaCorrectedData"
                data-test="copy-corrected-to-custom"
            />
          </div>

          <div class="judge-modal__json-editor">
            <label class="judge-modal__json-label" for="custom-json-textarea">
              Custom datapoint JSON
            </label>
            <textarea
                id="custom-json-textarea"
                v-model="customJson"
                class="judge-modal__json-textarea"
                spellcheck="false"
                data-test="custom-json-textarea"
            ></textarea>
          </div>

          <div class="judge-modal__section-actions">
            <PrimeButton
                label="ACCEPT CUSTOM"
                @click=""
                :disabled="isMutating || !isCustomJsonValid"
                data-test="accept-custom-button"
            />
            <span v-if="!isCustomJsonValid && customJson.trim().length > 0" class="judge-modal__validation-hint">
              Custom JSON must be valid JSON.
            </span>
          </div>
        </section>
      </div>

      <!-- Bottom-right: Next datapoint selection & patch error -->
      <div class="judge-modal__grid-cell judge-modal__grid-bottom-right">
        <section class="judge-modal__section" data-test="next-datapoint-section">
          <h3 class="judge-modal__section-title">Next datapoint</h3>

          <div class="judge-modal__next-toggle">
            <InputSwitch
                id="only-unreviewed-toggle"
                v-model="onlyShowUnreviewed"
                data-test="only-unreviewed-toggle"
            />
            <label for="only-unreviewed-toggle" class="judge-modal__toggle-label">
              Only show unreviewed
            </label>
          </div>

          <div class="judge-modal__next-select-container">
            <Select
                v-model="selectedNextDataPointTypeId"
                :options="nextDataPointOptions"
                optionLabel="label"
                optionValue="value"
                :filter="true"
                placeholder="Select next datapoint"
                data-test="next-datapoint-select"
            >
              <template #option="slotProps">
                <div class="judge-modal__next-option">
                  <span>{{ slotProps.option.label }}</span>
                  <i
                      v-if="slotProps.option.reviewed"
                      class="pi pi-check judge-modal__next-option-icon"
                      aria-hidden="true"
                  ></i>
                </div>
              </template>
            </Select>
            <PrimeButton
                label="GO TO"
                @click="goToSelectedDataPoint"
                :disabled="!selectedNextDataPointTypeId"
                data-test="go-to-datapoint-button"
            />
          </div>
        </section>

        <section v-if="patchError" class="judge-modal__section">
          <Message severity="error" data-test="judge-modal-patch-error">
            {{ patchError }}
          </Message>
        </section>
      </div>
    </div>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import type Keycloak from 'keycloak-js';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import Message from 'primevue/message';
import InputSwitch from 'primevue/inputswitch';

import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

// TanStack Query composables (names assumed from your description)
// import { useDatasetReviewQuery, datasetReviewKeys } from '@/composables/useDatasetReviewQuery.ts';
// import { useMutation, useQueryClient } from '@tanstack/vue-query';
// import {ReviewDetailsPatch} from "@clients/qaservice";

// ===== Props & emits =====

const props = defineProps<{
  datasetReviewId: string;
  dataPointTypeId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

// v-model:visible from parent
const isOpen = defineModel<boolean>('isOpen');

// ===== API clients (aligned with your style) =====

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const datasetReviewControllerApi = apiClientProvider.apiClients.datasetReviewController;
// Assumption: a backend client for data-point endpoint exists:
const dataPointControllerApi = apiClientProvider.apiClients.dataPointController;

// ===== Dataset review via shared query composable =====

// const {
//   data: datasetReview,
//   isLoading: isDatasetReviewLoading,
//   error: datasetReviewError,
// } = useDatasetReviewQuery(() => props.datasetReviewId);

const mockDataPointsById: Record<string, DataPointDetail> = {
  'mock-dp-1': {
    value: 12345,
    quality: 'High',
    comment: 'Mock original datapoint for QA comparison.',
    dataSource: {
      fileName: 'Sustainability_Report_2023.pdf',
      page: 12,
    },
  },
  'mock-dp-2': {
    value: 987,
    quality: 'Medium',
    comment: 'Mock original datapoint for custom acceptance.',
    dataSource: {
      fileReference: 'MockSource-REF-77',
      pageRange: '4-6',
    },
  },
};

// Create mock dataset review with entries that will be populated
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
              dataSource: {
                fileName: 'Sustainability_Report_2023.pdf',
                page: 13,
              },
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
              dataSource: {
                fileName: 'Sustainability_Report_2023.pdf',
                pageRange: '14-15',
              },
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
    },
    qaReporters: [
      {
        reporterUserId: 'mock-user-1',
        reporterUserName: 'Jane QA',
        reporterEmailAddress: 'jane.qa@example.com',
      },
      {
        reporterUserId: 'mock-user-2',
        reporterUserName: 'Alex QA',
        reporterEmailAddress: 'alex.qa@example.com',
      },
    ],
  } as any;
}

const datasetReview = ref(createMockDatasetReview());
const isDatasetReviewLoading = ref(false);
const datasetReviewError = ref(null);

// const queryClient = useQueryClient();
//
// const patchMutation = useMutation({
//   mutationFn: async (variables: { dataPointType: string; payload: ReviewDetailsPatch }) => {
//     return datasetReviewControllerApi.patchReviewDetails(
//         props.datasetReviewId,
//         variables.dataPointType,
//         variables.payload
//     );
//   },
//   onSuccess: () => {
//     // Invalidate dataset review detail query
//     // queryClient.invalidateQueries({ queryKey: datasetReviewKeys.detail(props.datasetReviewId) });
//   },
// });

// const isMutating = computed(() => patchMutation.isPending.value);
const isMutating = ref(true);
const patchError = ref<string | null>(null);

// ===== Current datapoint selection (local state) =====

const currentDataPointTypeId = ref<string>(props.dataPointTypeId);

watch(
    () => props.dataPointTypeId,
    (newVal) => {
      currentDataPointTypeId.value = newVal;
      resetStateForCurrentDataPoint();
    }
);

// Helper: current datapoint meta from datasetReview
const currentDataPointMeta = computed<any | null>(() => {
  if (!datasetReview.value?.dataPoints) return null;
  return datasetReview.value.dataPoints[currentDataPointTypeId.value] ?? null;
});

// ===== Original datapoint (GET /data-points/{dataPointId}) =====

interface DataPointSourceInfo {
  fileName?: string | null;
  fileReference?: string | null;
  page?: string | number | null;
  pageRange?: string | null;
  tagName?: string | null;
  publicationDate?: string | null;
  [key: string]: unknown;
}

interface DataPointDetail {
  value?: unknown;
  quality?: unknown;
  comment?: unknown;
  dataSource?: DataPointSourceInfo | null;
  // other arbitrary keys depending on datapoint type
  [key: string]: unknown;
}

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
    // Adjust to actual client method & response type
    // const response = await dataPointControllerApi.getDataPoint(meta.dataPointId);
    // const jsonString = response.data.dataPoint;
    // originalData.value = JSON.parse(jsonString) as DataPointDetail;
    originalData.value = mockDataPointsById['mock-dp-1'];
  } catch (error) {
    console.error('Failed to load original datapoint', error);
    originalError.value = error;
  } finally {
    isOriginalLoading.value = false;
  }
}

const originalJsonPretty = computed(() => {
  if (!originalData.value) return '';
  try {
    return JSON.stringify(originalData.value, null, 2);
  } catch {
    return '';
  }
});

// Reload original datapoint whenever currentDataPointTypeId changes
watch(
    () => currentDataPointTypeId.value,
    () => {
      loadOriginalDataPoint().catch((error) => console.error(error));
    },
    { immediate: true }
);

// ===== QA reports (corrected datapoints) =====

interface QaReport {
  qaReportId: string;
  verdict: string;
  correctedData: string;
  reporterUserId: string;
}

interface QaReporter {
  reporterUserId: string;
  reporterUserName?: string | null;
  reporterEmailAddress?: string | null;
}

const filteredQaReports = computed<QaReport[]>(() => {
  const meta = currentDataPointMeta.value;
  if (!meta?.qaReports) return [];
  // Hide reports where verdict === 'QaAccepted'
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

const currentQaCorrectedJsonPretty = computed(() => {
  if (!currentQaCorrectedData.value) return '';
  try {
    return JSON.stringify(currentQaCorrectedData.value, null, 2);
  } catch {
    return '';
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

// ===== Custom datapoint JSON editor =====

const customJson = ref<string>('');

const isCustomJsonValid = computed<boolean>(() => {
  if (!customJson.value.trim()) return false;
  try {
    JSON.parse(customJson.value); // must be valid JSON
    return true;
  } catch {
    return false;
  }
});

function copyOriginalToCustom(): void {
  if (!originalData.value) return;
  customJson.value = JSON.stringify(originalData.value, null, 2);
}

function copyCorrectedToCustom(): void {
  if (!currentQaCorrectedData.value) return;
  customJson.value = JSON.stringify(currentQaCorrectedData.value, null, 2);
}

// ===== Next datapoint dropdown =====

const onlyShowUnreviewed = ref<boolean>(true);
const selectedNextDataPointTypeId = ref<string | null>(null);

interface NextDatapointOption {
  label: string;
  value: string;
  reviewed: boolean;
}

const nextDataPointOptions = computed<NextDatapointOption[]>(() => {
  if (!datasetReview.value?.dataPoints) return [];
  const options: NextDatapointOption[] = [];

  // Keep order from keys of datasetReview.dataPoints
  const entries = Object.entries(datasetReview.value.dataPoints) as [string, any][];

  for (const [dataPointType, meta] of entries) {
    const reviewed = meta.acceptedSource !== null; // acceptedSource !== null means decision made
    if (onlyShowUnreviewed.value && reviewed) continue;

    options.push({
      label: dataPointType, // for now KPI label == dataPointTypeId
      value: dataPointType,
      reviewed,
    });
  }
  return options;
});


// Jump to selected datapoint via button click
function goToSelectedDataPoint(): void {
  if (!selectedNextDataPointTypeId.value) return;
  const currentSelection = selectedNextDataPointTypeId.value;
  currentDataPointTypeId.value = currentSelection;
  const options = nextDataPointOptions.value;
  const currentIndex = options.findIndex(opt => opt.value === currentSelection);
  if (currentIndex !== -1 && currentIndex + 1 < options.length) {
    selectedNextDataPointTypeId.value = options[currentIndex + 1].value;
  } else if (options.length > 0) {
    selectedNextDataPointTypeId.value = options[0].value;
  } else {
    selectedNextDataPointTypeId.value = null;
  }

  resetStateForCurrentDataPoint();
}

// ===== Accept actions (PATCH) =====

// async function onAcceptOriginal(): Promise<void> {
//   if (!currentDataPointMeta.value) return;
//   patchError.value = null;
//   try {
//     await patchMutation.mutateAsync({
//       dataPointType: currentDataPointTypeId.value,
//       payload: {
//         acceptedSource: 'Original',
//       },
//     });
//     handleAfterSuccessfulPatch();
//   } catch (error: any) {
//     console.error('Failed to accept original datapoint', error);
//     patchError.value = error?.message ?? 'Failed to save decision.';
//   }
// }
//
// async function onAcceptReport(): Promise<void> {
//   if (!currentDataPointMeta.value || !currentQaReport.value) return;
//   patchError.value = null;
//   try {
//     await patchMutation.mutateAsync({
//       dataPointType: currentDataPointTypeId.value,
//       payload: {
//         acceptedSource: 'Qa',
//         reporterUserIdOfAcceptedQaReport: currentQaReport.value.reporterUserId,
//       },
//     });
//     handleAfterSuccessfulPatch();
//   } catch (error: any) {
//     console.error('Failed to accept QA report', error);
//     patchError.value = error?.message ?? 'Failed to save decision.';
//   }
// }
//
// async function onAcceptCustom(): Promise<void> {
//   if (!currentDataPointMeta.value || !isCustomJsonValid.value) return;
//   patchError.value = null;
//   try {
//     await patchMutation.mutateAsync({
//       dataPointType: currentDataPointTypeId.value,
//       payload: {
//         acceptedSource: 'Custom',
//         customDataPoint: customJson.value,
//       },
//     });
//     handleAfterSuccessfulPatch();
//   } catch (error: any) {
//     console.error('Failed to accept custom datapoint', error);
//     patchError.value = error?.message ?? 'Failed to save decision.';
//   }
// }

function handleAfterSuccessfulPatch(): void {
  // After mutation success, datasetReview query is invalidated by onSuccess.
  // Behavior: if next datapoint selected -> jump there, else close.
  if (selectedNextDataPointTypeId.value) {
    currentDataPointTypeId.value = selectedNextDataPointTypeId.value;
    resetStateForCurrentDataPoint();
  } else {
    handleClose();
  }
}

// ===== Helpers / lifecycle =====

function resetStateForCurrentDataPoint(): void {
  patchError.value = null;
  currentQaReportIndex.value = 0;
  customJson.value = '';
  loadOriginalDataPoint().catch((error) => console.error(error));
}

function handleClose(): void {
  selectedNextDataPointTypeId.value = null;
  onlyShowUnreviewed.value = true;
  patchError.value = null;

  isOpen.value = false;
  emit('close');
}

onMounted(() => {
  if (currentDataPointMeta.value) {
    loadOriginalDataPoint().catch((error) => console.error(error));
  }
});
</script>

<style scoped lang="scss">

.judge-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin: 0;
  padding: 0;
}

.judge-modal__header :deep(.p-dialog-title) {
  margin: 0;
  padding: 0;
}

.judge-modal__kpi-name {
  font-weight: var(--font-weight-semibold);
  margin: 0;
  padding: 0;
}

.judge-modal__content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
}

.judge-modal__grid-cell {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__grid-top-left {
  grid-column: 1;
  grid-row: 1;
}

.judge-modal__grid-top-right {
  grid-column: 2;
  grid-row: 1;
}

.judge-modal__grid-bottom-left {
  grid-column: 1;
  grid-row: 2;
}

.judge-modal__grid-bottom-right {
  grid-column: 2;
  grid-row: 2;
}

.judge-modal__section {
  border: 1px solid #e3e2df;
  border-radius: 4px;
  padding: var(--spacing-sm);
  background-color: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__section-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  margin-top: 0;
  margin-bottom: var(--spacing-sm);
}

.judge-modal__section-header-with-nav {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.judge-modal__section-header-with-nav .judge-modal__section-title {
  margin-bottom: 0;
  flex: 1;
}

.judge-modal__qa-nav {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-xs);
  flex-shrink: 0;
}

.judge-modal__qa-nav :deep(.p-button) {
  padding: 0 !important;
  height: auto !important;
  min-height: auto !important;
  line-height: 1 !important;
}

.judge-modal__qa-nav :deep(.p-button-icon) {
  font-size: 0.875rem;
}

.judge-modal__qa-nav :deep(.p-button:hover),
.judge-modal__qa-nav :deep(.p-button:focus) {
  background-color: transparent !important;
}

.judge-modal__qa-reporter {
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
}

.judge-modal__qa-counter {
  font-weight: normal;
  font-size: var(--font-size-sm);
  margin-left: var(--spacing-xs);
}

.judge-modal__datatable {
  width: 100%;
  border-spacing: 0;
  border-collapse: collapse;

  tr {
    border-bottom: 1px solid #e3e2df;

    th {
      width: 8rem;
      padding-right: var(--spacing-md);
      vertical-align: middle;
      text-align: left;
    }

    td {
      padding: 0.25rem 0;
      vertical-align: middle;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.judge-modal__json-view {
  margin-top: var(--spacing-sm);
}

.judge-modal__json-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--spacing-xxs);
  display: block;
}

.judge-modal__json-pre {
  max-height: 10rem;
  overflow: auto;
  background-color: #f6f6f6;
  padding: var(--spacing-xs);
  border-radius: 4px;
  font-size: var(--font-size-xs);
  white-space: pre;
}

.judge-modal__section-actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.judge-modal__section-actions :deep(.p-button) {
  padding: 0.4rem 0.8rem !important;
  font-size: var(--font-size-sm) !important;
  height: auto !important;
  min-height: auto !important;
}

.judge-modal__multiline {
  white-space: pre-wrap;
}

.judge-modal__qa-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.judge-modal__qa-reporter {
  flex: 1;
  text-align: center;
  font-weight: var(--font-weight-medium);
}

.judge-modal__no-qa {
  font-style: italic;
  color: #666;
}

.judge-modal__section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.judge-modal__placeholder-label {
  font-size: var(--font-size-xs);
  color: #999;
}

.judge-modal__custom-actions {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xs);
}

.judge-modal__custom-actions :deep(.p-button) {
  font-size: var(--font-size-xs);
  padding: 0.4rem 0.6rem !important;
  white-space: nowrap;
}

.judge-modal__json-editor {
  margin-top: var(--spacing-xs);
}

.judge-modal__json-textarea {
  width: 100%;
  min-height: 10rem;
  max-height: 20rem;
  padding: var(--spacing-xs);
  font-family: monospace;
  font-size: var(--font-size-xs);
  border-radius: 4px;
  border: 1px solid #ccc;
  resize: vertical;
  overflow: auto;
}

.judge-modal__validation-hint {
  font-size: var(--font-size-xs);
  color: #c0392b;
}

.judge-modal__next-toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.judge-modal__toggle-label {
  font-size: var(--font-size-sm);
}

.judge-modal__next-select {
  margin-bottom: var(--spacing-sm);
}

.judge-modal__next-select-container {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.judge-modal__next-select-container :deep(.p-select) {
  flex: 1;
}

.judge-modal__next-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.judge-modal__next-option-icon {
  color: var(--primary-color);
}

.judge-modal__next-hint {
  font-size: var(--font-size-xs);
  color: #666;
}

.judge-modal__loading,
.judge-modal__error,
.judge-modal__subloading,
.judge-modal__suberror {
  font-size: var(--font-size-sm);
}

.nowrap {
  white-space: nowrap;
}
</style>
