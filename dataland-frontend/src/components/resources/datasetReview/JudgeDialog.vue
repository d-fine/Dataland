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
import { AcceptedDataPointSource, DataPointJudgement } from '@clients/qaservice';
import { useGetDataPointByIdQuery } from '@/api-queries/backend/data-point/useGetDataPointByIdQuery.ts';
import {
  useGetDocumentMetaInfoByCompanyIdQuery,
} from "@/api-queries/document-manager/document/useGetDocumentMetaInfoQuery.ts";
import {DocumentMetaInfoResponse} from "@clients/documentmanager";

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
}>();

const emit = defineEmits<{
  close: [];
}>();

// v-model:visible from parent
const isOpen = defineModel<boolean>('isOpen');

// ===== Dataset review =====

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
  return meta.qaReports as QaReport[];
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

const companyIdRef = computed<string | undefined>(() => datasetJudgement.value?.companyId);

const {
  data: allDocumentMetaInfo,
} = useGetDocumentMetaInfoByCompanyIdQuery(companyIdRef);

const availableDocuments = computed<DocumentOption[]>(() => {
  const docs = allDocumentMetaInfo?.value ?? [];
  return docs
    .filter((doc: DocumentMetaInfoResponse) =>
      doc.reportingPeriod == null || doc.reportingPeriod === datasetJudgement.value?.reportingPeriod
    )
    .map((doc: DocumentMetaInfoResponse) => {
      const label = doc.documentName ?? doc.documentId;
      return {
        label: label,
        value: label,
        dataSource: {
          fileName: label,
          fileReference: doc.documentId,
          publicationDate: doc.publicationDate ?? null,
        },
      };
    });
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

function markCurrentAsReviewed(source: AcceptedDataPointSource): void {
  if (datasetJudgement.value?.dataPoints?.[currentDataPointTypeId.value]) {
    datasetJudgement.value.dataPoints[currentDataPointTypeId.value].acceptedSource = source;
  }
}

function onAcceptClick(source: AcceptedDataPointSource): void {
  isMutating.value = true;
  patchError.value = null;
  markCurrentAsReviewed(source);
  isMutating.value = false;
  goToSelectedDataPoint();
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
