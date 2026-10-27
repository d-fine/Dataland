<template>
  <DataTable
    :value="rows"
    :scrollable="!props.flat"
    class="activities-data-table activities-edit-table"
    data-test="activities-edit-table"
  >
    <ColumnGroup v-if="!props.flat" type="header">
      <Row>
        <Column
          header=""
          :frozen="true"
          alignFrozen="left"
          :colspan="2"
          class="frozen-row-header border-right"
          style="background-color: #fff"
        />
        <Column
          v-for="group of props.groups"
          :key="group.key"
          :header="group.label"
          :colspan="group.fields.length"
          class="group-row-header"
        />
        <Column header="" :frozen="true" alignFrozen="right" class="col-actions" style="background-color: #fff" />
      </Row>
      <Row>
        <Column header="Activity" :frozen="true" alignFrozen="left" class="frozen-row-header" />
        <Column header="NACE Code(s)" :frozen="true" alignFrozen="left" class="frozen-row-header border-right" />
        <Column
          v-for="col of columns"
          :key="col.path"
          :header="col.header"
          :class="groupColumnCssClasses(col, 'non-frozen-header')"
        />
        <Column header="" :frozen="true" alignFrozen="right" class="col-actions" style="background-color: #fff" />
      </Row>
    </ColumnGroup>

    <Column
      field="activityName"
      header="Activity"
      :frozen="!props.flat"
      alignFrozen="left"
      v-bind="activityColumnProps"
    >
      <template #body="{ data }">
        <span data-test="activity-name">{{ humanizedActivityName(data.activityName) }}</span>
      </template>
    </Column>
    <Column field="naceCodes" header="NACE Code(s)" :frozen="!props.flat" alignFrozen="left" v-bind="naceColumnProps">
      <template #body="{ data }">
        <ul :class="{ 'unstyled-ul-list': props.flat }" data-test="nace-codes">
          <li v-for="code of data.naceCodes ?? []" :key="code">{{ code }}</li>
        </ul>
      </template>
    </Column>

    <Column v-for="col of columns" :key="col.path" :header="col.header" v-bind="mainColumnProps(col)">
      <template #body="{ index }">
        <div v-if="col.type === 'amount'" class="amount-cell">
          <InputNumber
            :modelValue="rows[index]!.share?.absoluteShare?.amount ?? null"
            :maxFractionDigits="2"
            fluid
            class="amount-input"
            :invalid="currencyWithoutAmount(rows[index]!)"
            v-tooltip.top="currencyWithoutAmount(rows[index]!) ? absoluteShareHint : ''"
            :inputId="`amount-${index}`"
            :aria-label="`${col.header} for ${humanizedActivityName(rows[index]!.activityName)}`"
            data-test="amount-input"
            @update:modelValue="(v: number | null) => updateCell(rows, index, 'share.absoluteShare.amount', v)"
          />
          <Select
            :modelValue="rows[index]!.share?.absoluteShare?.currency ?? null"
            :options="currencyList"
            optionLabel="label"
            optionValue="value"
            filter
            placeholder="Cur."
            class="currency-select"
            :invalid="amountWithoutCurrency(rows[index]!)"
            v-tooltip.top="amountWithoutCurrency(rows[index]!) ? absoluteShareHint : ''"
            :inputId="`currency-${index}`"
            :aria-label="`Currency for ${humanizedActivityName(rows[index]!.activityName)}`"
            data-test="currency-select"
            @update:modelValue="(v: string | null) => updateCell(rows, index, 'share.absoluteShare.currency', v)"
          >
            <template #value="{ value, placeholder }">{{ value ?? placeholder }}</template>
          </Select>
        </div>
        <InputNumber
          v-else-if="col.type === 'percent'"
          :modelValue="getNumber(rows[index]!, col.path)"
          suffix=" %"
          :min="0"
          :max="100"
          :maxFractionDigits="2"
          fluid
          :inputId="`${col.path}-${index}`"
          :aria-label="`${col.header} for ${humanizedActivityName(rows[index]!.activityName)}`"
          :data-test="'percent-' + col.path"
          @update:modelValue="(v: number | null) => updateCell(rows, index, col.path, v)"
        />
        <Select
          v-else
          :modelValue="(rows[index]![col.path] as string | null | undefined) ?? null"
          :options="yesNoOptions"
          showClear
          fluid
          :inputId="`${col.path}-${index}`"
          :aria-label="`${col.header} for ${humanizedActivityName(rows[index]!.activityName)}`"
          :data-test="'yesno-' + col.path"
          @update:modelValue="(v: string | null) => updateCell(rows, index, col.path, v)"
        />
      </template>
    </Column>

    <Column
      header=""
      :frozen="!props.flat"
      alignFrozen="right"
      class="col-actions"
      :headerClass="props.flat ? 'horizontal-headers-size border-bottom' : ''"
      :bodyClass="props.flat ? 'border-bottom' : ''"
    >
      <template #body="{ index }">
        <PrimeButton
          icon="pi pi-trash"
          severity="danger"
          text
          rounded
          aria-label="Delete activity"
          data-test="delete-activity"
          @click="deleteRow(rows, index)"
        />
      </template>
    </Column>
    <template #empty>
      <div class="empty-activities" data-test="empty-activities">
        <span>No activities have been added yet.</span>
        <PrimeButton
          label="Add first activity"
          icon="pi pi-plus"
          size="small"
          data-test="add-first-activity-button"
          @click="openAddActivitySelect"
        />
      </div>
    </template>
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
        ref="addActivitySelectRef"
        v-model="activityToAdd"
        :options="activityOptions"
        optionLabel="label"
        optionValue="value"
        filter
        placeholder="+ Add activity"
        class="add-activity-select"
        :overlayStyle="{ width: '300px' }"
        overlayClass="add-activity-overlay"
        inputId="add-activity-select"
        aria-label="Add activity"
        data-test="add-activity-select"
        @update:modelValue="onAddSelected"
      />
    </template>
  </ExtendedDataPointFormFieldDialog>
</template>

<script setup lang="ts">
import { computed, inject, ref, toRaw } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Select from 'primevue/select';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
import Tooltip from 'primevue/tooltip';
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
  type ActivityField,
  type ActivityFieldGroup,
  type ActivityRow,
  addRow,
  amountWithoutCurrency,
  currencyWithoutAmount,
  deleteRow,
  type KpiType,
  kpiTypeLabels,
  updateCell,
} from '@/utils/ActivityListHelpers.ts';

type EditColumn = ActivityField & { groupIndex: number };

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
  groups: ActivityFieldGroup[];
  /** Renders the table without group header row and frozen columns, like the non-aligned activities view modal. */
  flat?: boolean;
}>();

const kpiType = inject<KpiType | undefined>('activityKpiType', undefined) ?? 'revenue';
const kpiLabel = kpiTypeLabels[kpiType] ?? kpiTypeLabels.revenue;

const rows = ref<ActivityRow[]>(structuredClone(toRaw((props.extendedDataPointObject.value ?? []) as ActivityRow[])));
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();
const activityToAdd = ref<string | undefined>(undefined);
const addActivitySelectRef = ref<{ show: (isFocus?: boolean) => void }>();

const vTooltip = Tooltip;
const absoluteShareHint = 'Please provide both an amount and a currency, or leave both empty.';
const allActivityOptions = Object.entries(ActivityName).map(([value, label]) => ({ value, label }));
const activityOptions = computed(() => {
  const used = new Set(rows.value.map((row) => row.activityName));
  return allActivityOptions.filter((option) => !used.has(option.value));
});
const currencyList = getDataset(DropdownDatasetIdentifier.CurrencyCodes).sort((a, b) => a.label.localeCompare(b.label));
const yesNoOptions = ['Yes', 'No'];

const columns = computed<EditColumn[]>(() =>
  props.groups.flatMap((group) =>
    group.fields.map((field, groupIndex) => ({
      ...field,
      header: field.header.replaceAll('{kpi}', kpiLabel),
      groupIndex,
    }))
  )
);

const activityColumnProps = computed(() =>
  props.flat
    ? { headerClass: 'col-activity headers-bg border-bottom', bodyClass: 'col-activity headers-bg border-bottom' }
    : { class: 'col-activity', bodyClass: 'headers-bg', headerClass: 'horizontal-headers-size' }
);

const naceColumnProps = computed(() =>
  props.flat
    ? { headerClass: 'col-nace-codes headers-bg border-bottom', bodyClass: 'col-nace-codes headers-bg border-bottom' }
    : { class: 'col-nace-codes', bodyClass: 'headers-bg', headerClass: 'horizontal-headers-size' }
);

/**
 * @param col the column definition
 * @returns the css bindings of a main column, matching the ones of the view modal
 */
function mainColumnProps(col: EditColumn): Record<string, string> {
  const widthClass = col.type === 'amount' ? 'col-amount' : '';
  if (props.flat) {
    const classes = `horizontal-headers-size border-bottom ${widthClass}`;
    return { headerClass: classes, bodyClass: classes };
  }
  return {
    class: groupColumnCssClasses(col, widthClass),
    bodyClass: col.type === 'amount' ? '' : 'col-value',
    headerClass: 'horizontal-headers-size',
  };
}

/**
 * @param col the column definition
 * @param additionalClasses additional classes to add
 * @returns the classes of the column, adding a left border to the first column of each group
 */
function groupColumnCssClasses(col: EditColumn, additionalClasses = ''): string {
  return col.groupIndex === 0 ? `first-group-column ${additionalClasses}` : additionalClasses;
}

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
function getNumber(row: ActivityRow, path: string): number | null {
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
  if (name && !rows.value.some((row) => row.activityName === name)) addRow(rows.value, name);
  activityToAdd.value = undefined;
}

/**
 * Opens the add activity dropdown, so that a first activity can be added to an empty list.
 */
function openAddActivitySelect(): void {
  addActivitySelectRef.value?.show(true);
}

const percentValid = (row: ActivityRow): boolean =>
  columns.value
    .filter((col) => col.type === 'percent')
    .every(({ path }) => {
      const value = getNumber(row, path);
      return value === null || (value >= 0 && value <= 100);
    });

const isPageValid = computed<boolean>(
  () =>
    rows.value.every(
      (row) => !!row.activityName && percentValid(row) && !amountWithoutCurrency(row) && !currencyWithoutAmount(row)
    ) &&
    (extendedDialogRef.value?.isPageValid ?? true)
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
.activities-edit-table :deep(.p-datatable-frozen-column) {
  z-index: 2;
}

.activities-edit-table :deep(.p-datatable-thead > tr > th.p-datatable-frozen-column) {
  z-index: 3;
}

.activities-edit-table :deep(.p-datatable-tbody > tr > td.col-actions) {
  background-color: #fff;
  width: 3.5rem;
  min-width: 3.5rem;
  padding-left: 0.25rem;
  padding-right: 0.25rem;
  text-align: center;
}

.activities-edit-table :deep(.col-amount) {
  width: 20rem;
  min-width: 20rem;
}

.amount-cell {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.amount-cell .amount-input {
  flex: 1 1 auto;
  min-width: 6rem;
}

.amount-cell .amount-input :deep(.p-inputnumber-input) {
  width: 100%;
}

.amount-cell .currency-select {
  flex: 0 0 6.5rem;
  width: 6.5rem;
}

ul.unstyled-ul-list {
  padding: 0;
  margin: 0;

  li {
    padding: 0;
    margin: 0;
    list-style: none;
  }
}

.add-activity-select {
  width: 300px;
}

.add-activity-select :deep(.p-select-label) {
  text-align: left;
}

.empty-activities {
  position: sticky;
  left: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  width: fit-content;
}

.edit-footer {
  margin: var(--spacing-md) 0;
}
</style>

<style>
/* The overlay is teleported to the body, so it cannot be styled with scoped styles. */
.add-activity-overlay .p-select-option {
  white-space: normal;
}
</style>
