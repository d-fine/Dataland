<template>
  <h4>Value</h4>
  <div class="currency-value-fields">
    <div>
      <InputNumber v-model="value" placeholder="Insert Value" data-test="currency-value-input" fluid />
    </div>
    <div>
      <Select
        v-model="currency"
        placeholder="Currency"
        :options="currencyList"
        :maxFractionDigits="2"
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
    v-model:chosenQuality="chosenQuality"
    v-model:selectedDocument="selectedDocument"
    v-model:insertedComment="insertedComment"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    v-model:insertedPage="insertedPage"
  />
</template>

<script setup lang="ts">
import { ref, inject, watchEffect, watch } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import { buildApiBody, parseValue } from '@/components/resources/dataTable/conversion/Utils.ts';
import Select from 'primevue/select';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets.ts';

const props = defineProps({
  value: [String, Number, null],
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
});

const emit = defineEmits(['update:apiBody']);

const value = ref<number | null>(parseValue(props.value));
const currency = ref<string | null>(null);
const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = inject<string>('reportingPeriod');
const dataPointTypeId = inject<string>('dataPointTypeId');
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);
const currencyList = getDataset(DropdownDatasetIdentifier.CurrencyCodes).sort((currencyA, currencyB) =>
  currencyA.label.localeCompare(currencyB.label)
);

watch(
  () => props.value,
  (newVal) => {
    value.value = parseValue(newVal);
  }
);

watchEffect(() => {
  apiBody.value = buildApiBody(
    value.value,
    chosenQuality.value,
    selectedDocument.value,
    insertedComment.value,
    insertedPage.value,
    selectedDocumentMeta.value,
    companyId!,
    reportingPeriod!,
    dataPointTypeId!,
    currency.value
  );
  emit('update:apiBody', apiBody.value);
});
</script>

<style scoped>
.currency-value-fields {
  display: flex;
  gap: var(--spacing-md);
}
</style>
