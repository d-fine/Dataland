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
  NextDataPointOption,
  QaReport,
  QaReporter,
} from '@/components/resources/datasetReview/JudgeDialogTypes.ts';
import { useDatasetReviewQuery } from '@/api-queries/qa-service/dataset-judgement/useDatasetReviewQuery.ts';
import { AcceptedDataPointSource, DataPointJudgement, QaReportDataPointVerdict } from '@clients/qaservice';
import { useGetDataPointByIdQuery } from '@/api-queries/backend/data-point/useGetDataPointByIdQuery.ts';
import { usePatchJudgmentDetailsForADatapointMutation } from '@/api-queries/qa-service/dataset-judgement/usePatchJudgmentDetailsForADatapointMutation.ts';

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
  nextDataPointOptions: NextDataPointOption[];
  availableDocuments?: DocumentOption[];
}>();

const emit = defineEmits<{
  close: [];
}>();

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
  ([typeId, meta, id]) => {
    console.log('JudgeDialog currentDataPointTypeId:', typeId);
    console.log('JudgeDialog matching meta:', meta);
    console.log('JudgeDialog currentDataPointId:', id);
    if (datasetJudgement.value?.dataPoints) {
      console.log('Available dataPointType keys:', Object.keys(datasetJudgement.value.dataPoints));
    }
  },
  { immediate: true }
);

// ===== Original datapoint =====

const {
  data: originalDataPoint,
  isPending: isOriginalLoading,
  error: originalError,
} = useGetDataPointByIdQuery(currentDataPointId, {
  enabled: computed(() => !!currentDataPointId.value),
});

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
  const meta = currentDatapointJudgement.value;
  if (!meta?.qaReports) return [];
  return (meta.qaReports as QaReport[]).filter((r) => r.verdict !== QaReportDataPointVerdict.QaAccepted);
});

const verdictBadge = computed<{ label: string; cssClass: string } | null>(() => {
  const meta = currentDatapointJudgement.value;
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
const selectedNextDataPointTypeId = ref<string>(findNextUnreviewedDataPoint(currentDataPointTypeId.value));

function isDataPointJudged(meta: any): boolean {
  return meta.acceptedSource != null;
}

const nextDataPointOptions = computed<NextDataPointOption[]>(() => {
  const options: NextDataPointOption[] = [];
  for (const row of props.nextDataPointOptions) {
    if (!row.dataPointTypeId) continue;
    const meta = datasetJudgement.value?.dataPoints?.[row.dataPointTypeId];
    const reviewed = meta ? isDataPointJudged(meta) : false;
    if (onlyShowUnreviewed.value && reviewed) continue;
    options.push({
      label: row.label,
      dataPointTypeId: row.dataPointTypeId,
      reviewed,
    });
  }
  return options;
});

function findNextUnreviewedDataPoint(currentDataPointTypeId: string): string {
  const ids = props.nextDataPointOptions.map((row) => row.dataPointTypeId).filter(Boolean) as string[];
  const currentIndex = ids.indexOf(currentDataPointTypeId);
  const total = ids.length;
  for (let offset = 1; offset < total; offset++) {
    const id = ids[(currentIndex + offset) % total];
    const meta = datasetJudgement.value?.dataPoints?.[id];
    if (!meta || !isDataPointJudged(meta)) return id;
  }
  return currentDataPointTypeId;
}

function goToSelectedDataPoint(): void {
  const targetId = selectedNextDataPointTypeId.value;
  if (targetId == null) return;
  currentDataPointTypeId.value = targetId;
  resetStateForCurrentDataPoint();
  selectedNextDataPointTypeId.value = findNextUnreviewedDataPoint(targetId);
}

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

function afterSuccessfulPatch(): void {
  if (selectedNextDataPointTypeId.value) {
    const targetId = selectedNextDataPointTypeId.value;
    currentDataPointTypeId.value = targetId;
    resetStateForCurrentDataPoint();
    selectedNextDataPointTypeId.value = findNextUnreviewedDataPoint(targetId);
  } else {
    isOpen.value = false;
    emit('close');
  }
}
function buildCustomDataPointJson(): string {
  const { value, quality, comment, pages, document } = customFormData.value;

  const documentOption = availableDocuments.value?.find((doc) => doc.value === document) ?? null;
  const documentDataSource = documentOption?.dataSource ?? null;

  let dataSource: DataPointDetail['dataSource'] | null;
  if (documentDataSource) {
    dataSource = { ...documentDataSource, ...(pages ? { page: pages } : {}) };
  } else if (pages) {
    dataSource = { page: pages };
  } else {
    dataSource = null;
  }

  const data: DataPointDetail = {
    ...(value && { value }),
    ...(quality && { quality }),
    ...(comment && { comment }),
    ...(dataSource && Object.keys(dataSource).length > 0 && { dataSource }),
  };

  return Object.keys(data).length > 0 ? JSON.stringify(data, null, 2) : DEFAULT_CUSTOM_JSON;
}

function acceptOriginalDatapoint(): void {
  if (!currentDataPointTypeId.value) return;

  patchJudgementDetail(
    {
      judgmentId: props.datasetReviewId,
      dataPointTypeId: currentDataPointTypeId.value,
      details: {
        acceptedSource: AcceptedDataPointSource.Original,
        reporterUserIdOfAcceptedQaReport: undefined,
        customDataPoint: undefined,
      },
    },
    {
      onSuccess: () => {
        afterSuccessfulPatch();
      },
      onError: () => {
        console.log(
          'Error in patching datasetJudgement object for datapointId: ',
          currentDataPointTypeId.value,
          ' with AcceptedDataPointSource.Original.'
        );
      },
    }
  );
}
function acceptQaReportDatapoint(): void {
  if (!currentDataPointTypeId.value || !currentQaReport.value) return;

  patchJudgementDetail(
    {
      judgmentId: props.datasetReviewId,
      dataPointTypeId: currentDataPointTypeId.value,
      details: {
        acceptedSource: AcceptedDataPointSource.Qa,
        reporterUserIdOfAcceptedQaReport: currentQaReport.value.reporterUserId,
        customDataPoint: undefined,
      },
    },
    {
      onSuccess: () => {
        afterSuccessfulPatch();
      },
      onError: () => {
        console.log(
          'Error in patching datasetJudgement object for datapointId: ',
          currentDataPointTypeId.value,
          ' with AcceptedDataPointSource.Qa and reporterUserId: ',
          currentQaReport.value?.reporterUserId
        );
      },
    }
  );
}

function acceptCustomDatapoint(): void {
  if (!currentDataPointTypeId.value) return;

  const customDataPointJson = editModeEnabled.value ? customJson.value : buildCustomDataPointJson();

  patchJudgementDetail(
    {
      judgmentId: props.datasetReviewId,
      dataPointTypeId: currentDataPointTypeId.value,
      details: {
        acceptedSource: AcceptedDataPointSource.Custom,
        reporterUserIdOfAcceptedQaReport: undefined,
        customDataPoint: customDataPointJson,
      },
    },
    {
      onSuccess: () => {
        afterSuccessfulPatch();
      },
      onError: () => {
        console.log(
          'Error in patching datasetJudgement object for datapointType: ',
          currentDataPointTypeId.value,
          ' with AcceptedDataPointSource.Custom.'
        );
      },
    }
  );
}

function resetStateForCurrentDataPoint(): void {
  currentQaReportIndex.value = 0;
}

function setCustomFormForCurrentDataPoint(meta: DataPointJudgement | null): void {
  if (meta?.acceptedSource === AcceptedDataPointSource.Custom && meta.customValue) {
    try {
      const prev = JSON.parse(meta.customValue) as DataPointDetail;
      customFormData.value = {
        value: String(prev.value ?? ''),
        quality: String(prev.quality ?? ''),
        document: String(prev.dataSource?.fileName ?? prev.dataSource?.fileReference ?? ''),
        pages: String(prev.dataSource?.page ?? ''),
        comment: String(prev.comment ?? ''),
      };
      customJson.value = JSON.stringify(prev, null, 2);
      return;
    } catch (e) {
      console.error('Failed to parse previously accepted custom datapoint JSON', e);
    }
  }
  customJson.value = DEFAULT_CUSTOM_JSON;
  customFormData.value = { ...DEFAULT_CUSTOM_FORM_DATA };
}
watch(currentDatapointJudgement, setCustomFormForCurrentDataPoint, { immediate: true });

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
