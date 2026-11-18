<template>
  <h4>Value</h4>
  <PrimeSelect
    v-model="dataPointValue"
    :options="options"
    optionLabel="label"
    optionValue="value"
    :showClear="true"
    placeholder="Select..."
    class="w-full"
    data-test="single-select"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { computed, inject, ref } from 'vue';
import PrimeSelect from 'primevue/select';
import { PcafGeneralCompanyMainPcafSectorOptions } from '@clients/backend';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import {
  buildApiBody,
  type ExtendedDataPointMetaInfoType,
  type ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

interface SelectOption {
  label: string;
  value: string;
}

/**
 * Turns a technical enum value (e.g. "RealEstate") into a readable label (e.g. "Real Estate").
 * @param value the technical enum value
 * @returns the readable label
 */
function humanize(value: string): string {
  return value.replace(/([a-z])([A-Z])/g, '$1 $2');
}

/**
 * @param enumObject the generated enum object
 * @returns the select options for all values of the enum
 */
function optionsFromEnum(enumObject: Record<string, string>): SelectOption[] {
  return Object.values(enumObject).map((value) => ({ label: humanize(value), value }));
}

const optionsByDataPointTypeId: Record<string, SelectOption[]> = {
  extendedEnumPcafMainSector: optionsFromEnum(PcafGeneralCompanyMainPcafSectorOptions),
};

const dataPointTypeId = inject<string | undefined>('dataPointTypeId', undefined);
const options = optionsByDataPointTypeId[dataPointTypeId ?? ''] ?? [];

const initialValue = props.extendedDataPointObject.value;
const dataPointValue = ref<string | null>(
  typeof initialValue === 'string'
    ? (options.find((option) => option.value === initialValue || option.label === initialValue)?.value ?? null)
    : null
);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();

const isPageValid = computed<boolean>(() => extendedDialogRef.value?.isPageValid ?? true);

/**
 * @returns the JSON-stringified API body including the extended meta info
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(dataPointValue.value, undefined, extendedDialogRef.value?.getFormData());
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
