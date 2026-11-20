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
import PrimeSelect from 'primevue/select';
import { PcafGeneralCompanyMainPcafSectorOptions, SfdrGeneralCompanyMainPcafSectorOptions } from '@clients/backend';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import {
  useOptionExtendedDataPoint,
  type SelectOption,
} from '@/components/resources/dataTable/modals/useOptionExtendedDataPoint.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

/**
 * @param enumObject the generated enum object
 * @returns the select options for all values of the enum, with readable labels (e.g. "RealEstate" -> "Real Estate")
 */
function optionsFromEnum(enumObject: Record<string, string>): SelectOption[] {
  return Object.values(enumObject).map((value) => ({ label: value.replace(/([a-z])([A-Z])/g, '$1 $2'), value }));
}

const { options, dataPointValue, selectedDocumentMeta, extendedDialogRef, isPageValid, buildApiBodyWithExtendedInfo } =
  useOptionExtendedDataPoint(props.extendedDataPointObject, {
    extendedEnumPcafMainSector: optionsFromEnum(PcafGeneralCompanyMainPcafSectorOptions),
    extendedEnumSfdrMainPcafSector: optionsFromEnum(SfdrGeneralCompanyMainPcafSectorOptions),
  });

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
