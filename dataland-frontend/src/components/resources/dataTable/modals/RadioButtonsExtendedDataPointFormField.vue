<template>
  <h4>Value</h4>
  <SelectButton
    v-model="dataPointValue"
    :options="options"
    optionLabel="label"
    optionValue="value"
    :allowEmpty="true"
    data-test="radio-buttons-select"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import SelectButton from 'primevue/selectbutton';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import {
  useOptionExtendedDataPoint,
  type SelectOption,
} from '@/components/resources/dataTable/modals/useOptionExtendedDataPoint.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const deviationOptions: SelectOption[] = [
  { label: 'Deviation', value: 'Deviation' },
  { label: 'No Deviation', value: 'NoDeviation' },
];
const exchangeStatusOptions: SelectOption[] = [
  { label: 'Listed', value: 'Listed' },
  { label: 'Unlisted', value: 'Unlisted' },
];

const { options, dataPointValue, selectedDocumentMeta, extendedDialogRef, isPageValid, buildApiBodyWithExtendedInfo } =
  useOptionExtendedDataPoint(
    props.extendedDataPointObject,
    {
      extendedEnumCompanyExchangeStatus: exchangeStatusOptions,
      extendedEnumSfdrCompanyExchangeStatus: exchangeStatusOptions,
    },
    deviationOptions
  );

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
