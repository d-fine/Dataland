<template>
  <DataTable data-test="dataTableTest" :value="mainColumnData" class="activities-data-table">
    <Column
      v-for="col in mainColumnDefinitions"
      :key="col.field"
      :field="col.field"
      :header="col.header"
      :headerClass="cellClass(col)"
      :bodyClass="cellClass(col)"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">
          {{ activityApiNameToHumanizedName(data.activity) }}
        </template>
        <template v-else-if="col.field === 'naceCodes'">
          <ul class="unstyled-ul-list">
            <li v-for="code in data.naceCodes" :key="code">{{ code }}</li>
          </ul>
        </template>
        <template v-else>
          {{ data[col.field] }}
        </template>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts" setup>
// @ts-nocheck
import { inject, ref, onMounted } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { activityApiNameToHumanizedName } from '@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames';
import { formatAmountWithCurrency, formatPercentageNumberAsString } from '@/utils/Formatter';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import type { EuTaxonomyAlignedActivity } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';

type NonAlignedActivityFieldValueObject = {
  activity: string;
  naceCodes: string[];
  revenue: string;
  revenuePercent: string;
};

type MainColumnDefinition = {
  field: string;
  header: string;
};

const dialogRef = inject('dialogRef') as DynamicDialogInstance;

const listOfRowContents = ref<EuTaxonomyAlignedActivity[]>([]);
const kpiKeyOfTable = ref('');
const columnHeaders = ref<{ [kpiKeyOfTable: string]: { [columnName: string]: string } }>({});
const mainColumnDefinitions = ref<MainColumnDefinition[]>([]);
const mainColumnData = ref<NonAlignedActivityFieldValueObject[]>([]);

const humanizeHeaderName = (key: string): string => {
  return columnHeaders.value[kpiKeyOfTable.value][key];
};

const cellClass = (col: MainColumnDefinition): string => {
  if (col.field === 'activity') {
    return 'col-activity headers-bg border-bottom';
  } else if (col.field === 'naceCodes') {
    return 'col-nace-codes headers-bg border-bottom';
  }
  return 'horizontal-headers-size border-bottom';
};

onMounted(() => {
  const dialogData = dialogRef.data as {
    listOfRowContents: Array<object | string>;
    kpiKeyOfTable: string;
    columnHeaders: { [kpiKeyOfTable: string]: { [columnName: string]: string } };
  };

  kpiKeyOfTable.value = dialogData.kpiKeyOfTable;
  columnHeaders.value = dialogData.columnHeaders;

  if (typeof dialogData.listOfRowContents[0] === 'string') {
    listOfRowContents.value = dialogData.listOfRowContents.map((o) => ({ [kpiKeyOfTable.value]: o }));
  } else {
    listOfRowContents.value = dialogData.listOfRowContents as EuTaxonomyAlignedActivity[];
  }

  mainColumnDefinitions.value = [
    { field: 'activity', header: humanizeHeaderName('activityName') },
    { field: 'naceCodes', header: humanizeHeaderName('naceCodes') },
    { field: 'kpi', header: humanizeHeaderName('kpi') },
    { field: 'kpiPercent', header: humanizeHeaderName('kpiPercent') },
  ];

  mainColumnData.value = listOfRowContents.value.map((activity) => ({
    activity: activity.activityName as string,
    naceCodes: activity.naceCodes as string[],
    kpi: formatAmountWithCurrency(activity.share?.absoluteShare),
    kpiPercent: formatPercentageNumberAsString(activity.share?.relativeShareInPercent),
  }));
});
</script>
