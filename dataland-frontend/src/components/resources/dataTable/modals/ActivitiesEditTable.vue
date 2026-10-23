<template>
  <DataTable :value="rows" scrollable scrollHeight="60vh" class="inline-edit-table" data-test="activities-edit-table">
    <Column header="Activity" frozen style="min-width: 22rem">
      <template #body="{ index }">
        <Select
          :modelValue="rows[index]!.activityName"
          :options="activityOptions"
          optionLabel="label"
          optionValue="value"
          filter
          placeholder="Select activity"
          fluid
          data-test="activity-select"
          @update:modelValue="(v: string | undefined) => changeActivity(rows, index, v)"
        />
      </template>
    </Column>
    <Column header="NACE codes" style="min-width: 18rem">
      <template #body="{ index }">
        <MultiSelect
          :modelValue="rows[index]!.naceCodes ?? []"
          :options="naceOptions(rows[index]!.activityName)"
          optionLabel="label"
          optionValue="value"
          :disabled="!rows[index]!.activityName"
          display="chip"
          placeholder="Select NACE codes"
          fluid
          data-test="nace-select"
          @update:modelValue="(v: string[]) => updateCell(rows, index, 'naceCodes', v)"
        />
      </template>
    </Column>
    <Column :header="props.amountHeader" style="min-width: 20rem">
      <template #body="{ index }">
        <div style="display: flex; gap: 0.5rem">
          <InputNumber
            :modelValue="rows[index]!.share?.absoluteShare?.amount ?? null"
            :maxFractionDigits="2"
            fluid
            data-test="amount-input"
            @update:modelValue="(v: number | null) => updateCell(rows, index, 'share.absoluteShare.amount', v)"
          />
          <Select
            :modelValue="rows[index]!.share?.absoluteShare?.currency ?? null"
            :options="currencyList"
            optionLabel="label"
            optionValue="value"
            filter
            showClear
            placeholder="Currency"
            style="width: 9rem"
            data-test="currency-select"
            @update:modelValue="(v: string | null) => updateCell(rows, index, 'share.absoluteShare.currency', v)"
          />
        </div>
      </template>
    </Column>
    <Column v-for="field in props.percentFields" :key="field.path" :header="field.header" style="min-width: 11rem">
      <template #body="{ index }">
        <InputNumber
          :modelValue="getPercent(rows[index]!, field.path)"
          suffix="%"
          :min="0"
          :max="100"
          :maxFractionDigits="2"
          fluid
          :data-test="'percent-' + field.path"
          @update:modelValue="(v: number | null) => updateCell(rows, index, field.path, v)"
        />
      </template>
    </Column>
    <Column v-for="field in props.yesNoFields" :key="field.path" :header="field.header" style="min-width: 9rem">
      <template #body="{ index }">
        <Select
          :modelValue="(rows[index]![field.path] as string | null | undefined) ?? null"
          :options="yesNoOptions"
          showClear
          fluid
          :data-test="'yesno-' + field.path"
          @update:modelValue="(v: string | null) => updateCell(rows, index, field.path, v)"
        />
      </template>
    </Column>
    <Column header="" frozen alignFrozen="right" style="min-width: 4rem">
      <template #body="{ index }">
        <PrimeButton
          icon="pi pi-trash"
          severity="danger"
          text
          aria-label="Delete activity"
          data-test="delete-activity"
          @click="deleteRow(rows, index)"
        />
      </template>
    </Column>
  </DataTable>
  <div class="add-row">
    <PrimeButton label="+ Add activity" link data-test="add-activity" @click="addRow(rows)" />
    <Select
      v-model="activityToAdd"
      :options="activityOptions"
      optionLabel="label"
      optionValue="value"
      filter
      placeholder="Search and add activity"
      style="min-width: 22rem"
      data-test="add-activity-select"
      @update:modelValue="onAddSelected"
    />
  </div>
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Select from 'primevue/select';
import MultiSelect from 'primevue/multiselect';
import InputNumber from 'primevue/inputnumber';
import PrimeButton from 'primevue/button';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import {
  buildApiBody,
  type ExtendedDataPointMetaInfoType,
  type ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';
import { ActivityName } from '@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames.ts';
import { convertSingleNaceCode } from '@/utils/NaceCodeConverter.ts';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets.ts';
import {
  type ActivityField,
  type ActivityRow,
  addRow,
  changeActivity,
  deleteRow,
  getNaceCodesForActivity,
  updateCell,
} from '@/utils/ActivityListHelpers.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
  amountHeader: string;
  percentFields: ActivityField[];
  yesNoFields: ActivityField[];
}>();

const rows = ref<ActivityRow[]>(JSON.parse(JSON.stringify(props.extendedDataPointObject.value ?? [])));
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();
const activityToAdd = ref<string | undefined>(undefined);

const activityOptions = Object.entries(ActivityName).map(([value, label]) => ({ value, label }));
const currencyList = getDataset(DropdownDatasetIdentifier.CurrencyCodes).sort((a, b) => a.label.localeCompare(b.label));
const yesNoOptions = ['Yes', 'No'];

/**
 * @param row the row
 * @param path dot separated path
 * @returns the numeric value at the path or null
 */
function getPercent(row: ActivityRow, path: string): number | null {
  let current: unknown = row;
  for (const key of path.split('.')) {
    current = (current as Record<string, unknown> | null | undefined)?.[key];
  }
  return typeof current === 'number' ? current : null;
}

/**
 * @param activityName the selected activity
 * @returns NACE options valid for the activity
 */
function naceOptions(activityName: string | undefined): { value: string; label: string }[] {
  return getNaceCodesForActivity(activityName).map((code) => ({ value: code, label: convertSingleNaceCode(code) }));
}

/**
 * Appends a row for the activity selected in the add dropdown.
 * @param name the selected activity api name
 */
function onAddSelected(name: string | undefined): void {
  if (name) addRow(rows.value, name);
  activityToAdd.value = undefined;
}

const percentValid = (row: ActivityRow): boolean =>
  props.percentFields.every(({ path }) => {
    const value = getPercent(row, path);
    return value === null || (value >= 0 && value <= 100);
  });

const isPageValid = computed<boolean>(
  () =>
    rows.value.every((row) => !!row.activityName && percentValid(row)) && (extendedDialogRef.value?.isPageValid ?? true)
);

/**
 * Builds the API body containing the whole list plus the extended meta info.
 * @returns the JSON-stringified body
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(rows.value, undefined, extendedDialogRef.value?.getFormData());
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>

<style scoped>
.inline-edit-table :deep(.p-datatable-tbody > tr > td) {
  padding: 0.25rem 0.5rem;
}

.inline-edit-table :deep(.p-select),
.inline-edit-table :deep(.p-multiselect),
.inline-edit-table :deep(.p-inputnumber-input) {
  background: transparent;
  border-color: transparent;
  box-shadow: none;
}

.inline-edit-table :deep(.p-select:not(.p-disabled):hover),
.inline-edit-table :deep(.p-select.p-focus),
.inline-edit-table :deep(.p-multiselect:not(.p-disabled):hover),
.inline-edit-table :deep(.p-multiselect.p-focus),
.inline-edit-table :deep(.p-inputnumber-input:hover),
.inline-edit-table :deep(.p-inputnumber-input:focus) {
  background: var(--p-surface-100);
  border-color: transparent;
  border-radius: var(--p-border-radius-md, 6px);
}

.inline-edit-table :deep(.p-datatable-frozen-column) {
  background: var(--p-surface-0, #fff);
  z-index: 2;
}

.inline-edit-table :deep(.p-disabled) {
  opacity: 0.6;
}

.add-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: var(--spacing-md) 0;
}
</style>
