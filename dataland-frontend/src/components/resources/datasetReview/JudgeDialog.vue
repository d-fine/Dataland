<template>
  <PrimeDialog
      id="judgeModal"
      :dismissable-mask="true"
      :modal="true"
      class="col-8"
      style="min-width: 60rem; max-width: 60rem"
      v-model:visible="isOpen"
      @hide="emit('close')"
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
      <div style="grid-column: 1; grid-row: 1">
        <section class="judge-modal__section" data-test="original-datapoint-section">
          <div class="judge-modal__section-header-with-nav">
            <h3 class="judge-modal__section-title">Original datapoint</h3>
          </div>

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
                <td>
                  <div class="judge-modal__cell-overflow">
                    <span class="judge-modal__cell-text">{{ originalData.value ?? '—' }}</span>
                    <button
                        v-if="isOverflowing(String(originalData.value ?? ''))"
                        class="judge-modal__overflow-btn"
                        @mouseenter="(e) => showPopover(e, String(originalData?.value ?? ''))"
                        @mouseleave="hidePopover"
                        aria-label="Show full value"
                    >+</button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>Quality</th>
                <td>
                  <div class="judge-modal__cell-overflow">
                    <span class="judge-modal__cell-text">{{ originalData.quality ?? '—' }}</span>
                    <button
                        v-if="isOverflowing(String(originalData.quality ?? ''))"
                        class="judge-modal__overflow-btn"
                        @mouseenter="(e) => showPopover(e, String(originalData?.quality ?? ''))"
                        @mouseleave="hidePopover"
                        aria-label="Show full quality"
                    >+</button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>Document</th>
                <td>
                  <div class="judge-modal__cell-overflow">
                    <span class="judge-modal__cell-text">
                      {{ originalData.dataSource?.fileName ?? originalData.dataSource?.fileReference ?? '—' }}
                    </span>
                    <button
                        v-if="isOverflowing(String(originalData.dataSource?.fileName ?? originalData.dataSource?.fileReference ?? ''))"
                        class="judge-modal__overflow-btn"
                        @mouseenter="(e) => showPopover(e, String(originalData?.dataSource?.fileName ?? originalData?.dataSource?.fileReference ?? ''))"
                        @mouseleave="hidePopover"
                        aria-label="Show full document"
                    >+</button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>Page(s)</th>
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
                <td>
                  <div class="judge-modal__cell-overflow">
                    <span class="judge-modal__cell-text judge-modal__cell-text--comment">{{ originalData.comment ?? '—' }}</span>
                    <button
                        v-if="isOverflowing(String(originalData.comment ?? ''))"
                        class="judge-modal__overflow-btn"
                        @mouseenter="(e) => showPopover(e, String(originalData?.comment ?? ''))"
                        @mouseleave="hidePopover"
                        aria-label="Show full comment"
                    >+</button>
                  </div>
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
                @click="onAcceptClick('Original')"
                :disabled="isMutating"
                data-test="accept-original-button"
            />
          </div>
        </section>
      </div>

      <!-- Top-right: Corrected datapoint (QA reports) -->
      <div style="grid-column: 2; grid-row: 1">
        <section class="judge-modal__section" data-test="corrected-datapoint-section">
          <div class="judge-modal__section-header-with-nav">
            <h3 class="judge-modal__section-title">
              Corrected datapoint
              <span v-if="filteredQaReports.length > 0" class="judge-modal__qa-counter">
                ({{ currentQaReportIndex + 1 }} / {{ filteredQaReports.length }})
              </span>
            </h3>

            <div class="judge-modal__qa-nav" :style="{ visibility: filteredQaReports.length > 0 ? 'visible' : 'hidden' }">
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

          <div v-if="filteredQaReports.length === 0">
            No QA reports available.
          </div>

          <div v-else>

            <div v-if="currentQaCorrectedData">
              <table class="judge-modal__datatable" aria-label="Corrected datapoint">
                <tbody>
                <tr>
                  <th>Value</th>
                  <td>
                    <div class="judge-modal__cell-overflow">
                      <span class="judge-modal__cell-text">{{ currentQaCorrectedData.value ?? '—' }}</span>
                      <button
                          v-if="isOverflowing(String(currentQaCorrectedData.value ?? ''))"
                          class="judge-modal__overflow-btn"
                          @mouseenter="(e) => showPopover(e, String(currentQaCorrectedData?.value ?? ''))"
                          @mouseleave="hidePopover"
                          aria-label="Show full value"
                      >+</button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>Quality</th>
                  <td>
                    <div class="judge-modal__cell-overflow">
                      <span class="judge-modal__cell-text">{{ currentQaCorrectedData.quality ?? '—' }}</span>
                      <button
                          v-if="isOverflowing(String(currentQaCorrectedData.quality ?? ''))"
                          class="judge-modal__overflow-btn"
                          @mouseenter="(e) => showPopover(e, String(currentQaCorrectedData?.quality ?? ''))"
                          @mouseleave="hidePopover"
                          aria-label="Show full quality"
                      >+</button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>Document</th>
                  <td>
                    <div class="judge-modal__cell-overflow">
                      <span class="judge-modal__cell-text">
                        {{
                          currentQaCorrectedData.dataSource?.fileName ??
                          currentQaCorrectedData.dataSource?.fileReference ??
                          '—'
                        }}
                      </span>
                      <button
                          v-if="isOverflowing(String(currentQaCorrectedData.dataSource?.fileName ?? currentQaCorrectedData.dataSource?.fileReference ?? ''))"
                          class="judge-modal__overflow-btn"
                          @mouseenter="(e) => showPopover(e, String(currentQaCorrectedData?.dataSource?.fileName ?? currentQaCorrectedData?.dataSource?.fileReference ?? ''))"
                          @mouseleave="hidePopover"
                          aria-label="Show full document"
                      >+</button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th>Page(s)</th>
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
                  <td>
                    <div class="judge-modal__cell-overflow">
                      <span class="judge-modal__cell-text judge-modal__cell-text--comment">{{ currentQaCorrectedData.comment ?? '—' }}</span>
                      <button
                          v-if="isOverflowing(String(currentQaCorrectedData.comment ?? ''))"
                          class="judge-modal__overflow-btn"
                          @mouseenter="(e) => showPopover(e, String(currentQaCorrectedData?.comment ?? ''))"
                          @mouseleave="hidePopover"
                          aria-label="Show full comment"
                      >+</button>
                    </div>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="judge-modal__section-actions">
            <PrimeButton
                label="ACCEPT REPORT"
                @click="onAcceptClick('Qa')"
                :disabled="isMutating || filteredQaReports.length === 0 || !currentQaReport"
                data-test="accept-report-button"
            />
          </div>
        </section>
      </div>

      <!-- Separator line -->
      <div class="judge-modal__separator"></div>

      <!-- Bottom-left: Custom datapoint -->
      <div style="grid-column: 1; grid-row: 3">
        <section class="judge-modal__section" data-test="custom-datapoint-section">
          <div class="judge-modal__section-header-row">
            <h3 class="judge-modal__section-title">Custom datapoint</h3>
            <div class="judge-modal__toggle">
              <ToggleSwitch
                  id="edit-mode-toggle"
                  v-model="editModeEnabled"
                  data-test="edit-mode-toggle"
              />
              <label for="edit-mode-toggle" class="judge-modal__toggle-label">
                JSON
              </label>
            </div>
          </div>

          <div class="judge-modal__custom-actions">
            <PrimeButton
                label="Copy original datapoint"
                variant="text"
                @click="copyOriginalToCustom"
                :disabled="!originalData"
                data-test="copy-original-to-custom"
            />
            <PrimeButton
                label="Copy corrected datapoint"
                variant="text"
                @click="copyCorrectedToCustom"
                :disabled="!currentQaCorrectedData"
                data-test="copy-corrected-to-custom"
            />
          </div>

          <!-- View mode: Display as editable form -->
          <div v-if="!editModeEnabled" class="judge-modal__form-table">
            <div class="judge-modal__form-row">
              <label for="custom-value-field" class="judge-modal__form-label">Value</label>
              <InputText
                  id="custom-value-field"
                  v-model="customFormData.value"
                  class="judge-modal__form-input"
                  placeholder="Select Value"
                  data-test="custom-value-field"
              />
            </div>

            <div class="judge-modal__form-row">
              <label class="judge-modal__form-label">Quality</label>
              <Select
                  v-model="customFormData.quality"
                  :options="qualityOptions"
                  option-label="label"
                  option-value="value"
                  placeholder="Select Quality"
                  class="judge-modal__form-select"
                  :pt="{
                    root: { style: { height: '1.75rem', display: 'flex', alignItems: 'center' } },
                    label: { style: { fontSize: 'var(--font-size-sm)', fontFamily: 'inherit', padding: '0 0.5rem', lineHeight: '1' } },
                  }"
                  data-test="custom-quality-field"
              />
            </div>

            <div class="judge-modal__form-row">
              <label for="custom-document-field" class="judge-modal__form-label">Document</label>
              <InputText
                  id="custom-document-field"
                  v-model="customFormData.document"
                  class="judge-modal__form-input"
                  placeholder="Select Document"
                  data-test="custom-document-field"
              />
            </div>

            <div class="judge-modal__form-row">
              <label for="custom-pages-field" class="judge-modal__form-label">Page(s)</label>
              <InputText
                  id="custom-pages-field"
                  v-model="customFormData.pages"
                  class="judge-modal__form-input"
                  placeholder="Select Page(s)"
                  data-test="custom-pages-field"
              />
            </div>

            <div class="judge-modal__form-row">
              <label for="custom-comment-field" class="judge-modal__form-label">Comment</label>
              <Textarea
                  id="custom-comment-field"
                  v-model="customFormData.comment"
                  class="judge-modal__form-textarea"
                  placeholder="Write a comment"
                  rows="2"
                  data-test="custom-comment-field"
              />
            </div>
          </div>

          <!-- Edit mode: Display as JSON editor -->
          <div v-else class="judge-modal__json-editor">
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
                @click="onAcceptClick('Custom')"
                :disabled="isMutating || !isCustomJsonValid"
                data-test="accept-custom-button"
            />
            <span v-if="editModeEnabled && !isCustomJsonValid && customJson.trim().length > 0" class="judge-modal__validation-hint">
              Custom JSON must be valid JSON.
            </span>
          </div>
        </section>
      </div>

      <!-- Bottom-right: Next datapoint selection & patch error -->
      <div style="grid-column: 2; grid-row: 3">
        <section class="judge-modal__section judge-modal__section--centered" data-test="next-datapoint-section">
          <h3 class="judge-modal__section-title">Next datapoint</h3>

          <div class="judge-modal__toggle">
            <ToggleSwitch
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
                <div class="judge-modal__next-option" :class="{ 'judge-modal__next-option--reviewed': slotProps.option.reviewed }">
                  <i
                      v-if="slotProps.option.reviewed"
                      class="pi pi-check judge-modal__next-option-icon judge-modal__next-option-icon--reviewed"
                      aria-hidden="true"
                  ></i>
                  <span>{{ slotProps.option.label }}</span>
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

    <Popover
        ref="overflowPopover"
        placement="top"
        class="judge-modal__overflow-popover"
        :pt="{ root: { style: { width: popoverWidth, padding: '0.2rem 0.2rem'} } }"
    >
      <div class="judge-modal__overflow-popover-content">{{ popoverText }}</div>
    </Popover>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import type Keycloak from 'keycloak-js';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import Message from 'primevue/message';
import ToggleSwitch from 'primevue/toggleswitch';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Popover from 'primevue/popover';

import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import {QualityOptions} from "@clients/backend";
import {humanizeStringOrNumber} from "@/utils/StringFormatter.ts";


// ===== Props & emits =====
const DEFAULT_CUSTOM_JSON = JSON.stringify(
    { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
    null,
    2
);

const DEFAULT_CUSTOM_FORM_DATA = {
  value: '',
  quality: '',
  document: '',
  pages: '',
  comment: '',
};

const qualityOptions = Object.values(QualityOptions).map((qualityOption) => ({
  label: humanizeStringOrNumber(qualityOption),
  value: qualityOption,
}))

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
    comment: 'Mock original datapoint for QA comparison. Test comment to check multiline display.',
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
  'mock-dp-3': {
    value: 'TWh_RENEWABLE_SOLAR_WIND_HYDRO_BIOMASS_GEOTHERMAL_2023_CONSOLIDATED_GROSS_NET_ADJUSTED',
    quality: 'Estimated_PreliminaryAudit_PendingFinalVerificationByExternalAuditorGmbH',
    comment: 'This value was extracted from the consolidated energy production appendix on pages 47 through 53 of the annual sustainability disclosure. The figure includes all renewable sources as defined under EU Taxonomy Article 10 and has been adjusted for grid losses according to the methodology described in footnote 23. Please cross-reference with the interim report published in Q2 before final acceptance.',
    dataSource: {
      fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
      pageRange: '47-53',
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
              dataSource: {
                fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
                pageRange: '47-53',
              },
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
              dataSource: {
                fileName: 'Annual_Sustainability_Disclosure_and_EU_Taxonomy_Alignment_Report_FY2023_Final_Audited_v3.pdf',
                pageRange: '51-52',
              },
            }),
            reporterUserId: 'mock-user-2',
          },
        ],
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
const isMutating = ref(false);
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
    originalData.value = mockDataPointsById[meta.dataPointId] ?? null;
  } catch (error) {
    console.error('Failed to load original datapoint', error);
    originalError.value = error;
  } finally {
    isOriginalLoading.value = false;
  }
}


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

// ===== Custom datapoint JSON editor =====

const editModeEnabled = ref<boolean>(false);
const customJson = ref<string>('');

interface CustomFormData {
  value: string;
  quality: string;
  document: string;
  pages: string;
  comment: string;
}

const customFormData = ref<CustomFormData>(DEFAULT_CUSTOM_FORM_DATA);

const isCustomJsonValid = computed<boolean>(() => {
  if (!editModeEnabled.value) {
    const f = customFormData.value;
    return [f.value, f.quality, f.document, f.pages, f.comment].some((v) => v.trim().length > 0);
  }
  if (!customJson.value.trim()) return false;
  try {
    JSON.parse(customJson.value);
    return true;
  } catch {
    return false;
  }
});


function formDataToJson(): void {
  const { value, quality, comment, document, pages } = customFormData.value;

  const dataSource: DataPointSourceInfo = {
    ...(document ? { fileName: document } : {}),
    ...(pages ? (pages.includes('-') ? { pageRange: pages } : { page: pages }) : {}),
  };

  const data: DataPointDetail = {
    ...(value && { value }),
    ...(quality && { quality }),
    ...(comment && { comment }),
    ...(Object.keys(dataSource).length > 0 && { dataSource }),
  };

  customJson.value = Object.keys(data).length > 0 ? JSON.stringify(data, null, 2) : DEFAULT_CUSTOM_JSON;
}

function jsonToFormData(): void {
  try {
    const parsed = JSON.parse(customJson.value) as DataPointDetail;
    const toStr = (v: unknown): string => (v === null || v === undefined ? '' : String(v));
    customFormData.value = {
      value: toStr(parsed.value),
      quality: toStr(parsed.quality),
      document: toStr(parsed.dataSource?.fileName ?? parsed.dataSource?.fileReference),
      pages: toStr(parsed.dataSource?.pageRange ?? parsed.dataSource?.page),
      comment: toStr(parsed.comment),
    };
  } catch {
    // invalid JSON, leave form as-is
  }
  customJson.value = DEFAULT_CUSTOM_JSON;
}

// Sync between form and JSON when toggling edit mode
watch(editModeEnabled, (newVal) => {
  if (newVal) {
    formDataToJson();
  } else {
    jsonToFormData();
  }
});

function copyOriginalToCustom(): void {
  if (!originalData.value) return;

  if (editModeEnabled.value) {
    customJson.value = JSON.stringify(originalData.value, null, 2);
  } else {
    customFormData.value = {
      value: String(originalData.value.value ?? ''),
      quality: String(originalData.value.quality ?? ''),
      document: String(originalData.value.dataSource?.fileName ?? originalData.value.dataSource?.fileReference ?? ''),
      pages: String(originalData.value.dataSource?.pageRange ?? originalData.value.dataSource?.page ?? ''),
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
      document: String(currentQaCorrectedData.value.dataSource?.fileName ?? currentQaCorrectedData.value.dataSource?.fileReference ?? ''),
      pages: String(currentQaCorrectedData.value.dataSource?.pageRange ?? currentQaCorrectedData.value.dataSource?.page ?? ''),
      comment: String(currentQaCorrectedData.value.comment ?? ''),
    };
  }
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


// Returns the next unreviewed datapoint after the given id in the full list
function findNextUnreviewedAfter(afterId: string): string | null {
  if (!datasetReview.value?.dataPoints) return null;
  const allEntries = Object.entries(datasetReview.value.dataPoints) as [string, any][];
  const startIndex = allEntries.findIndex(([key]) => key === afterId);
  const rotated = [...allEntries.slice(startIndex + 1), ...allEntries.slice(0, startIndex)];
  const [key] = rotated.find(([, meta]) => meta.acceptedSource === null) ?? [];
  return key ?? null;
}

// Jump to selected datapoint and reset local state
function goToSelectedDataPoint(): void {
  if (!selectedNextDataPointTypeId.value) return;
  const currentSelection = selectedNextDataPointTypeId.value;
  currentDataPointTypeId.value = currentSelection;
  selectedNextDataPointTypeId.value = findNextUnreviewedAfter(currentSelection);
  resetStateForCurrentDataPoint();
}
// Mark current datapoint as reviewed with the given source
// (without backend call, just local state update for immediate UI feedback)
function markCurrentAsReviewed(source: string): void {
  if (datasetReview.value?.dataPoints?.[currentDataPointTypeId.value]) {
    datasetReview.value.dataPoints[currentDataPointTypeId.value].acceptedSource = source;
  }
}

// Handler for accept buttons: mark current as reviewed, then go to next unreviewed
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

// Reset local state related to current datapoint when switching to a different datapoint
function resetStateForCurrentDataPoint(): void {
  patchError.value = null;
  currentQaReportIndex.value = 0;
  customJson.value = DEFAULT_CUSTOM_JSON;
  customFormData.value = DEFAULT_CUSTOM_FORM_DATA
}

// ===== Overflow popover =====

const OVERFLOW_THRESHOLD = 40;

const overflowPopover = ref<InstanceType<typeof Popover> | null>(null);
const popoverText = ref<string>('');
const popoverWidth = ref<string>('auto');

function isOverflowing(text: string): boolean {
  return text.length > OVERFLOW_THRESHOLD;
}

function showPopover(event: MouseEvent, text: string): void {
  const btn = event.currentTarget as HTMLElement;
  const td = btn.closest('td') as HTMLElement | null;
  const anchor = td ?? btn;

  // Match the width of the form inputs in the bottom-left container
  const dialog = btn.closest('.p-dialog') ?? btn.closest('#judgeModal') ?? document;
  const formInput = (dialog as Element).querySelector('.judge-modal__form-input') as HTMLElement | null;
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
  gap: 0;
  grid-template-rows: auto auto 20rem;
}

.judge-modal__separator {
  grid-column: 1 / -1;
  height: 2px;
  background-color: #e3e2df;
  margin: 0;
}


.judge-modal__section {
  border-radius: 4px;
  padding: var(--spacing-xs);
  background-color: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-top: 0;
  margin-bottom: var(--spacing-xs);
}

.judge-modal__section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs);

  > .judge-modal__section-title {
    margin-bottom: 0;
  }
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
  table-layout: fixed;

  tr {

    th {
      width: 8rem;
      padding-right: var(--spacing-md);
      vertical-align: middle;
      text-align: left;
      font-weight: normal;
      flex-shrink: 0;
    }

    td {
      padding: 0.4rem 0;
      vertical-align: middle;
      font-size: var(--font-size-sm);
      max-width: 0;
    }
  }
}

.judge-modal__cell-overflow {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  min-width: 0;
}

.judge-modal__cell-text {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.judge-modal__cell-text--comment {
  white-space: nowrap;
}

.judge-modal__overflow-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.1rem;
  height: 1.1rem;
  padding: 0;
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
  line-height: 1;
  color: #fff;
  background-color: #888;
  border: none;
  border-radius: 50%;
  cursor: default;
  user-select: none;

  &:hover {
    background-color: #555;
  }
}

.judge-modal__overflow-popover {
  // width is controlled via :pt="{ root: { style: { width: popoverWidth } } }"
}

.judge-modal__overflow-popover-content {
  font-size: var(--font-size-sm);
  white-space: pre-wrap;
  word-break: break-word;
  box-sizing: border-box;
}


.judge-modal__section-actions {
  margin-top: auto;
  padding-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}


.judge-modal__custom-actions {
  display: flex;
  gap: 0.25rem;
  margin: 0;
}

.judge-modal__custom-actions :deep(.p-button:last-child) {
  margin-left: auto;
}

.judge-modal__custom-actions :deep(.p-button) {
  font-size: var(--font-size-xs);
  padding: 0.4rem 0.6rem !important;
  white-space: nowrap;
  flex: 0 0 auto;
}

.judge-modal__json-editor {
  margin-top: var(--spacing-xs);
  flex: 1;
  min-height: 12rem;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}

.judge-modal__json-textarea {
  width: 100%;
  flex: 1;
  min-height: 0;
  height: 100%;
  padding: var(--spacing-xs);
  font-family: monospace;
  font-size: var(--font-size-xs);
  border-radius: 4px;
  border: 1px solid #ccc;
  resize: none;
  overflow: auto;
}

.judge-modal__form-table {
  display: flex;
  flex-direction: column;
  gap: 0;
  flex: 1;
  min-height: 12rem;
  margin-top: var(--spacing-xs);
}

.judge-modal__form-row {
  display: flex;
  align-items: flex-start;
  gap: 0;
  padding: 0.1rem 0;
  flex-shrink: 0;
}

.judge-modal__form-row:last-child {
  align-items: flex-start;
}

.judge-modal__form-label {
  width: 8rem;
  padding-right: var(--spacing-md);
  text-align: left;
  font-weight: normal;
  font-size: inherit;
  line-height: 2;
  flex-shrink: 0;
}

.judge-modal__form-row:last-child .judge-modal__form-label {
  line-height: 1;
  padding-top: 0.4rem;
}

.judge-modal__form-input {
  flex: 1;
  padding: 0.3rem 0.5rem !important;
  font-size: var(--font-size-sm) !important;
  height: 1.75rem !important;
}

.judge-modal__form-select {
  flex: 1;
}


.judge-modal__form-textarea {
  flex: 1;
  padding: 0.3rem 0.5rem !important;
  font-size: var(--font-size-sm) !important;
  resize: none;
}

.judge-modal__validation-hint {
  font-size: var(--font-size-xs);
  color: #c0392b;
}

.judge-modal__toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.judge-modal__toggle-label {
  font-size: var(--font-size-sm);
}


.judge-modal__next-select-container {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
}

.judge-modal__next-select-container :deep(.p-select) {
  flex: 1;
}

.judge-modal__next-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.judge-modal__next-option--reviewed {
  opacity: 0.45;
}

.judge-modal__next-option-icon--reviewed {
  color: #27ae60;
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

.judge-modal__section--centered {
  justify-content: center;
}
</style>
