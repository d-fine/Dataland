<template>
  <h4>Value</h4>
  <div class="currency-value-fields">
    <div>
      <InputNumber
        v-model="dataPointValue"
        placeholder="Insert Value"
        data-test="currency-value-input"
        :maxFractionDigits="2"
        fluid
      />
    </div>
    <div>
      <Select
        v-model="currency"
        placeholder="Currency"
        :options="currencyList"
        option-label="label"
        option-value="value"
        class="currency-select"
        data-test="currency"
        style="width: 10em"
        fluid
      />
    </div>
  </div>
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import {
  buildApiBody,
  type ExtendedDataPointType,
  type ExtendedDataPointTypeMetaInfo,
  parseValue,
} from '@/components/resources/dataTable/conversion/Utils.ts';
import Select from 'primevue/select';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const currencyList = getDataset(DropdownDatasetIdentifier.CurrencyCodes).sort((currencyA, currencyB) =>
  currencyA.label.localeCompare(currencyB.label)
);
const dataPointValue = ref<number | null>(parseValue(props.extendedDataPointObject.value));
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointTypeMetaInfo }>();
const currency = ref<string | undefined>(undefined);

onMounted(() => {
  currency.value = props.extendedDataPointObject.value
    ? String(props.extendedDataPointObject.value).replaceAll(/[^A-Za-z]+/g, '') || undefined
    : undefined;
});

/**
 * Reference to the extended dialog to get form data
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(dataPointValue.value, currency.value, extendedDialogRef.value?.getFormData());
}

defineExpose({ buildApiBodyWithExtendedInfo });
</script>

<style scoped>
.currency-value-fields {
  display: flex;
  gap: var(--spacing-md);
}
</style>
