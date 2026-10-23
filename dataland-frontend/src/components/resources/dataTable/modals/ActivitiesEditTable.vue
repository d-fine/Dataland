<template>
  <DataTable
    :value="rows"
    scrollable
    scrollHeight="60vh"
    class="activities-data-table"
    data-test="activities-edit-table"
  >
    <ColumnGroup type="header">
      <Row>
        <Column header="" frozen alignFrozen="left" :colspan="2" class="frozen-row-header border-right" />
        <Column header="" class="group-row-header" />
        <Column
          v-for="group of props.groups"
          :key="group.key"
          :header="group.label"
          :colspan="group.fields.length"
          class="group-row-header"
        />
        <Column header="" frozen alignFrozen="right" class="group-row-header" />
      </Row>
      <Row>
        <Column header="Activity" frozen alignFrozen="left" class="frozen-row-header" />
        <Column header="NACE Code(s)" frozen alignFrozen="left" class="frozen-row-header border-right" />
        <Column :header="props.amountHeader" class="non-frozen-header col-amount" />
        <Column
          v-for="field of allFields"
          :key="field.path"
          :header="field.header"
          :class="['non-frozen-header', field.type === 'percent' ? 'col-percentage' : '']"
        />
        <Column header="" frozen alignFrozen="right" class="non-frozen-header" />
      </Row>
    </ColumnGroup>
    <Column field="activityName" frozen alignFrozen="left" bodyClass="headers-bg col-activity" class="col-activity">
      <template #body="{ data }">
        <span data-test="activity-name">{{ humanizedActivityName(data.activityName) }}</span>
      </template>
    </Column>
    <Column field="naceCodes" frozen alignFrozen="left" bodyClass="headers-bg col-nace-codes" class="col-nace-codes">
      <template #body="{ data }">
        <ul class="unstyled-ul-list" data-test="nace-codes">
          <li v-for="code of data.naceCodes ?? []" :key="code">{{ code }}</li>
        </ul>
      </template>
    </Column>
    <Column bodyClass="col-amount" class="col-amount">
      <template #body="{ index }">
        <div class="amount-cell">
          <InputNumber
            :modelValue="rows[index]!.share?.absoluteShare?.amount ?? null"
            :maxFractionDigits="2"
            fluid
            class="amount-input"
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
            placeholder="Cur."
            class="currency-select"
            data-test="currency-select"
            @update:modelValue="(v: string | null) => updateCell(rows, index, 'share.absoluteShare.currency', v)"
          >
            <template #value="{ value, placeholder }">{{ value ?? placeholder }}</template>
          </Select>
        </div>
      </template>
    </Column>
    <Column
      v-for="field of allFields"
      :key="field.path"
      bodyClass="col-value"
      :class="field.type === 'percent' ? 'col-percentage' : 'col-value'"
    >
      <template #body="{ index }">
        <InputNumber
          v-if="field.type === 'percent'"
          :modelValue="getPercent(rows[index]!, field.path)"
          suffix="%"
          :min="0"
          :max="100"
          :maxFractionDigits="2"
          fluid
          :data-test="'percent-' + field.path"
          @update:modelValue="(v: number | null) => updateCell(rows, index, field.path, v)"
        />
        <Select
          v-else
          :modelValue="(rows[index]![field.path] as string | null | undefined) ?? null"
          :options="yesNoOptions"
          showClear
          fluid
          :data-test="'yesno-' + field.path"
          @update:modelValue="(v: string | null) => updateCell(rows, index, field.path, v)"
        />
      </template>
    </Column>
    <Column frozen alignFrozen="right">
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
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    inline
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
    class="edit-footer"
  >
    <template #toolbar-start>
      <Select
        v-model="activityToAdd"
        :options="activityOptions"
        optionLabel="label"
        optionValue="value"
        filter
        placeholder="+ Add activity"
        style="width: 14em"
        data-test="add-activity-select"
        @update:modelValue="onAddSelected"
      />
    </template>
  </ExtendedDataPointFormFieldDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Select from 'primevue/select';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
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
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets.ts';
import {
  type ActivityFieldGroup,
  type ActivityRow,
  addRow,
  deleteRow,
  updateCell,
} from '@/utils/ActivityListHelpers.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
  amountHeader: string;
  groups: ActivityFieldGroup[];
}>();

const rows = ref<ActivityRow[]>(JSON.parse(JSON.stringify(props.extendedDataPointObject.value ?? [])));
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();
const activityToAdd = ref<string | undefined>(undefined);

const activityOptions = Object.entries(ActivityName).map(([value, label]) => ({ value, label }));
const currencyList = getDataset(DropdownDatasetIdentifier.CurrencyCodes).sort((a, b) => a.label.localeCompare(b.label));
const yesNoOptions = ['Yes', 'No'];
const allFields = computed(() => props.groups.flatMap((group) => group.fields));

/**
 * @param apiName the api name of the activity
 * @returns the humanized activity name (falls back to the api name)
 */
function humanizedActivityName(apiName: string | undefined): string {
  return apiName ? ((ActivityName as Record<string, string>)[apiName] ?? apiName) : '';
}

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
 * Appends a row for the activity selected in the add dropdown.
 * @param name the selected activity api name
 */
function onAddSelected(name: string | undefined): void {
  if (name) addRow(rows.value, name);
  activityToAdd.value = undefined;
}

const percentValid = (row: ActivityRow): boolean =>
  allFields.value
    .filter((field) => field.type === 'percent')
    .every(({ path }) => {
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
.activities-data-table :deep(.p-inputnumber-input),
.activities-data-table :deep(.p-select) {
  width: 100%;
}

.activities-data-table :deep(.p-datatable-tbody > tr > td) {
  vertical-align: top;
  padding: 0.5rem;
}

.activities-data-table :deep(.p-datatable-frozen-column) {
  background: var(--tables-headers-bg);
  z-index: 2;
}

.activities-data-table :deep(.p-datatable-thead > tr > th.p-datatable-frozen-column) {
  z-index: 3;
}

.activities-data-table :deep(.col-amount) {
  width: 17rem;
  min-width: 17rem;
}

.amount-cell {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.25rem;
}

.amount-cell .amount-input {
  flex: 1 1 auto;
  min-width: 6rem;
}

.amount-cell .currency-select {
  flex: 0 0 7rem;
  width: 7rem;
}

ul.unstyled-ul-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.edit-footer {
  margin: var(--spacing-md) 0;
}
</style>
