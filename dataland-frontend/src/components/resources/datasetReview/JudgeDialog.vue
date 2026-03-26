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
        <span class="p-dialog-title">{{ currentDataPointLabel }}</span>
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
      <Message severity="error"> Failed to load dataset review.</Message>
    </div>

    <div v-else class="judge-modal__content">
      <!-- Top-left: Original datapoint -->
      <JudgeDialogTopSection
        title="Original datapoint"
        :data="originalData"
        :is-loading="isOriginalLoading"
        :is-loading-error="isOriginalLoadingError"
        :loading-error-object="originalErrorValue"
        accept-label="ACCEPT ORIGINAL"
        :accept-disabled="isMutating"
        accept-data-test="accept-original-button"
        data-test="original-datapoint-section"
        :show-nav="false"
        :nav-index="0"
        :is-accepted="currentDatapointJudgement?.acceptedSource === AcceptedDataPointSource.Original"
        @accept="onAcceptClick(AcceptedDataPointSource.Original)"
        @show-popover="showPopover"
        @hide-popover="hidePopover"
      />

      <!-- Top-right: Reviewed datapoint (QA reports) -->
      <JudgeDialogTopSection
        title="Reviewed datapoint"
        :data="currentQaCorrectedData"
        empty-text="No QA reports available."
        accept-label="ACCEPT REVIEWED"
        :accept-disabled="isMutating || filteredQaReports.length === 0 || !currentQaReport"
        accept-data-test="accept-report-button"
        data-test="corrected-datapoint-section"
        :show-nav="filteredQaReports.length > 0"
        :nav-index="currentQaReportIndex"
        :nav-count="filteredQaReports.length"
        :nav-label="currentQaReporterLabel"
        :is-accepted="
          currentDatapointJudgement?.acceptedSource === AcceptedDataPointSource.Qa &&
          currentDatapointJudgement?.reporterUserIdOfAcceptedQaReport === currentQaReport?.reporterUserId
        "
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
        :is-accepted="currentDatapointJudgement?.acceptedSource === AcceptedDataPointSource.Custom"
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
        @go-to="navigateToDataPoint(selectedNextDataPointTypeId)"
      />
    </div>

    <Popover ref="overflowPopover" placement="top" :pt="{ root: { style: { width: popoverWidth } } }">
      <div class="judge-modal__overflow-popover-content">{{ popoverText }}</div>
    </Popover>
  </PrimeDialog>
  <PopupConfirmationModal
    v-model:visible="isErrorModalVisible"
    :header="errorModalHeader"
    :message="errorModalMessage"
    :error-message="errorModalDetails"
    :is-loading="false"
    :is-success="false"
    :show-cancel-button="false"
    @confirm="isErrorModalVisible = false"
    @cancel="isErrorModalVisible = false"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import PrimeDialog from 'primevue/dialog';
import Message from 'primevue/message';
import Popover from 'primevue/popover';

import PopupConfirmationModal from '@/components/resources/popups/PopupConfirmationModal.vue';
import JudgeDialogTopSection from '@/components/resources/datasetReview/JudgeDialogTopSection.vue';
import JudgeDialogCustomSection from '@/components/resources/datasetReview/JudgeDialogCustomSection.vue';
import JudgeDialogNextSection from '@/components/resources/datasetReview/JudgeDialogNextSection.vue';
import type {
  CustomFormData,
  DataPointDetail,
  DocumentOption,
  NextDataPointOption,
  QaReport,
  QaReporter,
} from '@/components/resources/datasetReview/JudgeDialogTypes.ts';
import {
  parseDataPointJsonToFormData,
  parseFormDataToDataPointJson,
  transformDataPointDetailToFormData,
  DEFAULT_CUSTOM_JSON,
} from '@/utils/JudgeDialogUtils.ts';
import { useDatasetReviewQuery } from '@/api-queries/qa-service/dataset-judgement/useDatasetReviewQuery.ts';
import { AcceptedDataPointSource, type DataPointJudgement } from '@clients/qaservice';
import { useGetDataPointByIdQuery } from '@/api-queries/backend/data-point/useGetDataPointByIdQuery.ts';
import { usePatchJudgmentDetailsForADatapointMutation } from '@/api-queries/qa-service/dataset-judgement/usePatchJudgmentDetailsForADatapointMutation.ts';

// ===== Props & emits =====

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
  nextDataPointOptions: NextDataPointOption[];
  availableDocuments?: DocumentOption[];
}>();

const emit = defineEmits<{
  close: [];
}>();

// Error popover
const isErrorModalVisible = ref(false);
const errorModalHeader = ref('Error updating datapoint');
const errorModalMessage = ref('Failed to update datapoint judgement.');
const errorModalDetails = ref<string | undefined>(undefined);

// v-model:visible from parent
const isOpen = defineModel<boolean>('isOpen');
const availableDocuments = computed(() => props.availableDocuments ?? []);

// ===== Dataset review =====

const datasetJudgementId = computed(() => props.datasetReviewId);
const {
  data: datasetJudgement,
  isPending: isDatasetJudgementPending,
  isError: datasetReviewError,
} = useDatasetReviewQuery({ datasetJudgementId: datasetJudgementId });

// ===== Accept Button mutations  =====
const {
  mutate: patchJudgementDetail,
  isPending: isPatching,
  isError: isPatchError,
} = usePatchJudgmentDetailsForADatapointMutation();

const isMutating = computed(() => isPatching.value);
const patchError = computed(() =>
  isPatchError.value ? 'Failed to update datapoint judgement. Please try again.' : null
);

// ===== Current datapoint selection =====

const currentDataPointTypeId = ref<string>(props.dataPointTypeId);
const currentDataPointLabel = computed(() => {
  const option = props.nextDataPointOptions.find((opt) => opt.dataPointTypeId === currentDataPointTypeId.value);
  return option ? option.label : currentDataPointTypeId.value;
});
watch(
  () => props.dataPointTypeId,
  (newVal) => {
    currentDataPointTypeId.value = newVal;
    selectedNextDataPointTypeId.value = findNextUnreviewedDataPoint(currentDataPointTypeId.value);
    resetStateForCurrentDataPoint();
  }
);

const currentDatapointJudgement = computed<DataPointJudgement | null>(() => {
  if (!datasetJudgement.value?.dataPoints) return null;
  return datasetJudgement.value.dataPoints[currentDataPointTypeId.value] ?? null;
});

const currentDataPointId = computed(() => currentDatapointJudgement.value?.dataPointId ?? '');
console.log('currentDataPointId', currentDataPointId.value);

watch(
  [currentDataPointTypeId, currentDatapointJudgement, currentDataPointId],
  ([typeId, judgementMetaData, id]) => {
    console.log('JudgeDialog currentDataPointTypeId:', typeId);
    console.log('JudgeDialog matching judgement:', judgementMetaData);
    console.log('JudgeDialog currentDataPointId:', id);
    if (datasetJudgement.value?.dataPoints) {
      console.log('Available dataPointType keys:', Object.keys(datasetJudgement.value.dataPoints));
    }
  },
  { immediate: true }
);

// ===== Original datapoint =====

const isOriginalQueryEnabled = computed(() => !!currentDataPointId.value);

const {
  data: originalDataPoint,
  isPending: isOriginalPending,
  isError: isOriginalError,
  error: originalError,
} = useGetDataPointByIdQuery(currentDataPointId, {
  enabled: isOriginalQueryEnabled,
});

const isOriginalLoading = computed(() => isOriginalQueryEnabled.value && isOriginalPending.value);
const isOriginalLoadingError = computed(() => isOriginalQueryEnabled.value && isOriginalError.value);
const originalErrorValue = computed<Error | null>(() => (isOriginalLoadingError.value ? originalError.value : null));

const originalData = computed<DataPointDetail | null>(() => {
  const dp = originalDataPoint.value;
  if (!dp?.dataPoint) return null;

  try {
    return JSON.parse(dp.dataPoint) as DataPointDetail;
  } catch (e) {
    console.error('Failed to parse original datapoint JSON', e);
    return null;
  }
});

// ===== QA reports =====

const filteredQaReports = computed<QaReport[]>(() => {
  const judgementMetaData = currentDatapointJudgement.value;
  if (!judgementMetaData?.qaReports) return [];
  return judgementMetaData.qaReports as QaReport[];
});

const verdictBadge = computed<{ label: string; cssClass: string } | null>(() => {
  const judgementMetaData = currentDatapointJudgement.value;
  if (!judgementMetaData) return null;
  const allReports = (judgementMetaData.qaReports as QaReport[]) ?? [];
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

/**
 * Navigates to the previous QA report in the filtered list, if available.
 *
 * @returns Nothing.
 */
function goToPreviousReport(): void {
  if (currentQaReportIndex.value > 0) {
    currentQaReportIndex.value -= 1;
  }
}

/**
 * Navigates to the next QA report in the filtered list, if available.
 *
 * @returns Nothing.
 */
function goToNextReport(): void {
  if (currentQaReportIndex.value < filteredQaReports.value.length - 1) {
    currentQaReportIndex.value += 1;
  }
}

const editModeEnabled = ref<boolean>(false);
const customJson = ref<string>(DEFAULT_CUSTOM_JSON);
const customFormData = ref<CustomFormData>({ ...DEFAULT_CUSTOM_FORM_DATA });

/**
 * Copies the original datapoint values into the custom section
 * (either as JSON or into the structured form).
 *
 * @returns Nothing.
 */
function copyOriginalToCustom(): void {
  if (!originalData.value) return;

  if (editModeEnabled.value) {
    customJson.value = JSON.stringify(originalData.value, null, 2);
  } else {
    customFormData.value = transformDataPointDetailToFormData(originalData.value);
  }
}

/**
 * Copies the current QA-corrected datapoint values into the custom section
 * (either as JSON or into the structured form).
 *
 * @returns Nothing.
 */
function copyCorrectedToCustom(): void {
  if (!currentQaCorrectedData.value) return;

  if (editModeEnabled.value) {
    customJson.value = JSON.stringify(currentQaCorrectedData.value, null, 2);
  } else {
    customFormData.value = transformDataPointDetailToFormData(currentQaCorrectedData.value);
  }
}

// ===== Next datapoint =====

const onlyShowUnreviewed = ref<boolean>(true);
const selectedNextDataPointTypeId = ref<string>(findNextUnreviewedDataPoint(currentDataPointTypeId.value));

/**
 * Determines whether the given datapoint judgement has already been decided.
 *
 * @param judgementMetaData - Datapoint judgement metadata.
 * @returns True if the datapoint has an accepted source; otherwise false.
 */
function isDataPointJudged(judgementMetaData: DataPointJudgement): boolean {
  return judgementMetaData.acceptedSource != null;
}

const nextDataPointOptions = computed<NextDataPointOption[]>(() => {
  const options: NextDataPointOption[] = [];
  for (const row of props.nextDataPointOptions) {
    if (!row.dataPointTypeId) continue;
    const judgementMetaData = datasetJudgement.value?.dataPoints?.[row.dataPointTypeId];
    const reviewed = judgementMetaData ? isDataPointJudged(judgementMetaData) : false;
    if (onlyShowUnreviewed.value && reviewed) continue;
    options.push({
      label: row.label,
      dataPointTypeId: row.dataPointTypeId,
      reviewed,
    });
  }
  return options;
});

/**
 * Finds the next unreviewed datapoint type ID, starting after the current one
 * and wrapping around when necessary.
 *
 * @param currentDataPointTypeId - The datapoint type ID to start from.
 * @returns The next unreviewed datapoint type ID, or the current one if none found.
 */
function findNextUnreviewedDataPoint(currentDataPointTypeId: string): string {
  const ids = props.nextDataPointOptions.map((row) => row.dataPointTypeId).filter(Boolean);
  const currentIndex = ids.indexOf(currentDataPointTypeId);
  const total = ids.length;
  for (let offset = 1; offset < total; offset++) {
    const id = ids[(currentIndex + offset) % total];
    const judgementMetaData = datasetJudgement.value?.dataPoints?.[id];
    if (!judgementMetaData || !isDataPointJudged(judgementMetaData)) return id;
  }
  return currentDataPointTypeId;
}

/**
 * Navigates to the given datapoint type, resets local state, and advances the
 * "selected next" pointer to the next unreviewed datapoint after it.
 *
 * @param targetId - The datapoint type ID to navigate to.
 * @returns Nothing.
 */
function navigateToDataPoint(targetId: string): void {
  currentDataPointTypeId.value = targetId;
  resetStateForCurrentDataPoint();
  selectedNextDataPointTypeId.value = findNextUnreviewedDataPoint(targetId);
}

/**
 * Handles navigation and cleanup after a successful patch of a datapoint judgement.
 *
 * @returns Nothing.
 */
function afterSuccessfulPatch(): void {
  if (selectedNextDataPointTypeId.value) {
    navigateToDataPoint(selectedNextDataPointTypeId.value);
  } else {
    isOpen.value = false;
    emit('close');
  }
}

/**
 * Calls patchJudgementDetail for the current datapoint with the given details,
 * navigating on success and logging on error.
 *
 * @param acceptedSource - The accepted data point source.
 * @param reporterUserIdOfAcceptedQaReport - The reporter user ID of the accepted QA report, if applicable.
 * @param customDataPoint - The custom data point JSON string, if applicable.
 * @param errorLogMessage - Message logged when the patch fails.
 * @returns Nothing.
 */
function patchCurrentDatapoint(
  acceptedSource: AcceptedDataPointSource,
  reporterUserIdOfAcceptedQaReport: string | undefined,
  customDataPoint: string | undefined,
  errorLogMessage: string
): void {
  patchJudgementDetail(
    {
      judgmentId: props.datasetReviewId,
      dataPointTypeId: currentDataPointTypeId.value,
      details: { acceptedSource, reporterUserIdOfAcceptedQaReport, customDataPoint },
    },
    {
      onSuccess: () => {
        afterSuccessfulPatch();
      },
      onError: (err: Error) => {
        console.log(errorLogMessage, err);

        errorModalHeader.value = 'Failed to update datapoint judgement';
        errorModalMessage.value = 'Your decision could not be saved. Please try again.';
        errorModalDetails.value = err.message || 'Unknown error.';

        isErrorModalVisible.value = true;
      },
    }
  );
}

/**
 * Central handler for accepting a datapoint from a given source
 * (original / QA / custom).
 *
 * @param acceptedSource - The selected datapoint source to accept.
 * @returns Nothing.
 */
function onAcceptClick(acceptedSource: AcceptedDataPointSource): void {
  switch (acceptedSource) {
    case AcceptedDataPointSource.Original:
      acceptOriginalDatapoint();
      break;
    case AcceptedDataPointSource.Qa:
      acceptQaReportDatapoint();
      break;
    case AcceptedDataPointSource.Custom:
      acceptCustomDatapoint();
      break;
  }
}

/**
 * Accepts the original datapoint as the final judgement for the current datapoint type.
 *
 * @returns Nothing.
 */
function acceptOriginalDatapoint(): void {
  if (!currentDataPointTypeId.value) return;
  patchCurrentDatapoint(
    AcceptedDataPointSource.Original,
    undefined,
    undefined,
    `Error in patching datasetJudgement object for datapointId: ${currentDataPointTypeId.value} with AcceptedDataPointSource.Original.`
  );
}

/**
 * Accepts the currently selected QA report as the final judgement
 * for the current datapoint type.
 *
 * @returns Nothing.
 */
function acceptQaReportDatapoint(): void {
  if (!currentDataPointTypeId.value || !currentQaReport.value) return;
  patchCurrentDatapoint(
    AcceptedDataPointSource.Qa,
    currentQaReport.value.reporterUserId,
    undefined,
    `Error in patching datasetJudgement object for datapointId: ${currentDataPointTypeId.value} with AcceptedDataPointSource.Qa and reporterUserId: ${currentQaReport.value.reporterUserId}`
  );
}

/**
 * Accepts the custom datapoint (either from JSON or from the structured form)
 * as the final judgement for the current datapoint type.
 *
 * @returns Nothing.
 */
function acceptCustomDatapoint(): void {
  if (!currentDataPointTypeId.value) return;
  const documentOption = availableDocuments.value.find((doc) => doc.value === customFormData.value.document) ?? null;
  const customDataPointJson = editModeEnabled.value
    ? customJson.value
    : parseFormDataToDataPointJson(customFormData.value, documentOption);
  patchCurrentDatapoint(
    AcceptedDataPointSource.Custom,
    undefined,
    customDataPointJson,
    `Error in patching datasetJudgement object for datapointType: ${currentDataPointTypeId.value} with AcceptedDataPointSource.Custom.`
  );
}

/**
 * Resets transient state (e.g. current QA report index) for the active datapoint.
 *
 * @returns Nothing.
 */
function resetStateForCurrentDataPoint(): void {
  currentQaReportIndex.value = 0;
}

/**
 * Initializes the custom form / JSON for the currently selected datapoint
 * based on previously accepted custom judgement, if present.
 *
 * @param judgementMetaData - The current datapoint judgement metadata.
 * @returns Nothing.
 */
function setCustomFormForCurrentDataPoint(judgementMetaData: DataPointJudgement | null): void {
  if (judgementMetaData?.acceptedSource === AcceptedDataPointSource.Custom && judgementMetaData.customValue) {
    const parsed = parseDataPointJsonToFormData(judgementMetaData.customValue);
    if (parsed !== null) {
      customFormData.value = parsed;
      customJson.value = judgementMetaData.customValue;
      return;
    }
    console.error('Failed to parse previously accepted custom datapoint JSON');
  }
  customJson.value = DEFAULT_CUSTOM_JSON;
  customFormData.value = { ...DEFAULT_CUSTOM_FORM_DATA };
}

watch(currentDatapointJudgement, setCustomFormForCurrentDataPoint, { immediate: true });

// ===== Overflow popover =====

const overflowPopover = ref<InstanceType<typeof Popover> | null>(null);
const popoverText = ref<string>('');
const popoverWidth = ref<string>('auto');

/**
 * Shows the overflow popover for truncated content with a width
 * matching the corresponding table cell or form field.
 *
 * @param event - Mouse event from the trigger element.
 * @param text - Text content to display inside the popover.
 * @returns Nothing.
 */
function showPopover(event: MouseEvent, text: string): void {
  const btn = event.currentTarget as HTMLElement;
  const td = btn.closest('td') as HTMLElement | null;
  const anchor = td ?? btn;

  const dialog = btn.closest('.p-dialog') ?? btn.closest('#judgeModal') ?? document;
  const formInput = (dialog as Element).querySelector('.judge-modal__form-field');
  const width = formInput ? formInput.getBoundingClientRect().width : anchor.getBoundingClientRect().width;
  popoverWidth.value = `${width}px`;

  popoverText.value = text;
  overflowPopover.value?.show(event, anchor);
}

/**
 * Hides the overflow popover, if visible.
 *
 * @returns Nothing.
 */
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
