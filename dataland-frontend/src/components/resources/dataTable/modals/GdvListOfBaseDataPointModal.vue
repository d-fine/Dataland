<script setup lang="ts">
import { computed, inject, type Ref } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import {
  type GdvYearlyDecimalTimeseriesDataConfiguration,
  type MappedOptionalDecimal,
  type YearlyTimeseriesData,
} from "@/components/resources/dataTable/conversion/gdv/GdvYearlyDecimalTimeseriesData";

import DataTable from "primevue/datatable";
import Column from "primevue/column";
import ColumnGroup from "primevue/columngroup";
import Row from "primevue/row";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type BaseDataPoint } from "@/utils/DataPoint";

interface GdvListOfBaseDataPointDialogData {
  label: string;
  input: Array<BaseDataPoint<string>>;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const dialogData = computed(() => {
  return dialogRef?.value?.data as GdvListOfBaseDataPointDialogData;
});

const tableData = computed<GdvDecimalTimeseriesRow<KeyList>[]>(() => {
  const dialogDataValue = dialogData?.value;
  if (!dialogDataValue) {
    return [];
  }

  const generatedRows: GdvDecimalTimeseriesRow<KeyList>[] = [];

  return generatedRows;
});

const yearsForDisplay = computed<YearsForDisplay | undefined>(() => {
  const dialogDataValue = dialogData?.value;
  if (!dialogDataValue) {
    return undefined;
  }

  const data: YearsForDisplay = {
    allYearsSorted: [],
    reportingYear: dialogData.value.input.currentYear,
    numberOfHistoricalYears: 0,
    numberOfPrognosisYears: 0,
  };

  for (const year of Object.keys(dialogDataValue.input.yearlyData)) {
    const numericalYear = parseInt(year);
    if (isNaN(numericalYear)) {
      throw new Error(`Non-numerical year ${numericalYear} received in GdvYearlyDecimalTimeseriesModal`);
    }
    data.allYearsSorted.push(numericalYear);
    if (numericalYear > data.reportingYear) {
      data.numberOfPrognosisYears++;
    } else if (numericalYear < data.reportingYear) {
      data.numberOfHistoricalYears++;
    }
  }

  if (data.allYearsSorted.indexOf(data.reportingYear) == -1) {
    data.allYearsSorted.push(data.reportingYear);
  }

  data.allYearsSorted = data.allYearsSorted.sort((a, b) => a - b);

  return data;
});
</script>

<template>
  <DataTable :value="tableData">
    <Column field="value" header="Values" headerStyle="width: 15vw;"> </Column>
  </DataTable>
</template>
