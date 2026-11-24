<template>
  <h4>Value</h4>
  <Select
    v-model="assuranceValue"
    :options="options"
    optionLabel="label"
    optionValue="value"
    showClear
    placeholder="Select assurance level"
    fluid
    inputId="assurance-select"
    aria-label="Assurance level"
    data-test="assurance-select"
  />
  <h4>Provider</h4>
  <InputText
    v-model="provider"
    placeholder="Insert provider"
    fluid
    id="assurance-provider"
    aria-label="Assurance provider"
    data-test="assurance-provider-input"
  />
  <h4>Comment</h4>
  <InputText
    v-model="comment"
    placeholder="Insert comment"
    fluid
    id="assurance-comment"
    aria-label="Assurance comment"
    data-test="assurance-comment-input"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
    :hideQualityAndComment="true"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Select from 'primevue/select';
import InputText from 'primevue/inputtext';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type {
  ExtendedDataPointMetaInfoType,
  ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';
import { AssuranceDataPointValueEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const options = Object.values(AssuranceDataPointValueEnum).map((value) => ({
  label: humanizeStringOrNumber(value),
  value,
}));

const initialValue = props.extendedDataPointObject.value;
const assuranceValue = ref<AssuranceDataPointValueEnum | null>(
  typeof initialValue === 'string'
    ? (options.find((option) => option.value === initialValue || option.label === initialValue)?.value ?? null)
    : null
);
const provider = ref<string>(props.extendedDataPointObject.provider ?? '');
const comment = ref<string>(props.extendedDataPointObject.comment ?? '');
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();

const isPageValid = computed<boolean>(() => !!assuranceValue.value && (extendedDialogRef.value?.isPageValid ?? true));

/**
 * @returns the JSON-stringified assurance data point (value, provider, comment and data source, never a quality)
 */
function buildApiBodyWithExtendedInfo(): string {
  const dataSource = extendedDialogRef.value?.getFormData().dataSource;
  return JSON.stringify({
    value: assuranceValue.value,
    ...(provider.value.trim() ? { provider: provider.value.trim() } : {}),
    ...(comment.value.trim() ? { comment: comment.value.trim() } : {}),
    ...(dataSource?.fileReference
      ? { dataSource: { fileReference: dataSource.fileReference, page: dataSource.page } }
      : {}),
  });
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
