<script setup generic="KeyList extends string" lang="ts">
import { computed, inject, type Ref } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import {
  type EsgQuestionnaireYearlyDecimalTimeseriesDataConfiguration,
  type MappedOptionalDecimal,
  type YearlyTimeseriesData,
} from '@/components/resources/dataTable/conversion/esg-questionnaire/EsgQuestionnaireYearlyDecimalTimeseriesData';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
import { formatNumberToReadableFormat } from '@/utils/Formatter';

interface EsgQuestionnaireYearlyDecimalTimeseriesDialogData<T extends string> {
  label: string;
  input: YearlyTimeseriesData<MappedOptionalDecimal<T>>;
  options: EsgQuestionnaireYearlyDecimalTimeseriesDataConfiguration<T>;
}

type EsgQuestionnaireDecimalTimeseriesRow<T> = {
  key: T;
  label: string;
  unitSuffix: string;
} & { [key: string]: string };

interface YearsForDisplay {
  reportingYear: number;
  allYearsSorted: number[];
  numberOfHistoricalYears: number;
  numberOfPrognosisYears: number;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const dialogData = computed<EsgQuestionnaireYearlyDecimalTimeseriesDialogData<KeyList> | undefined>(() => {
  return dialogRef?.value?.data as EsgQuestionnaireYearlyDecimalTimeseriesDialogData<KeyList>;
});

const tableData = computed<EsgQuestionnaireDecimalTimeseriesRow<KeyList>[]>(() => {
  const dialogDataValue = dialogData?.value;
  if (!dialogDataValue) {
    return [];
  }

  const generatedRows: EsgQuestionnaireDecimalTimeseriesRow<KeyList>[] = [];

  for (const [key, element] of Object.entries(dialogDataValue.options)) {
    const typedElement = element as {
      label: string;
      unitSuffix: string;
    };

    generatedRows.push({
      key: key as KeyList,
      label: typedElement.label,
      unitSuffix: typedElement.unitSuffix,
    });
  }

  for (const [year, data] of Object.entries(dialogDataValue.input.yearlyData)) {
    for (const row of generatedRows) {
      const rowValueForYear = data[row.key];
      if (rowValueForYear) {
        row[year] = `${formatNumberToReadableFormat(rowValueForYear)} ${row.unitSuffix}`.trim();
      } else {
        row[year] = '';
      }
    }
  }

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
      throw new Error(`Non-numerical year ${numericalYear} received in EsgQuestionnaireYearlyDecimalTimeseriesModal`);
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

  data.allYearsSorted.sort((a, b) => a - b);

  return data;
});

/**
 * Calculates the CSS class for the column with the given year.
 * Used to highlight the current year and put a border between the KPI label and the data
 * @param year the year to compute the css classes for
 * @returns the computed css classes
 */
function getClassForTableRow(year: number): string[] {
  if (year == yearsForDisplay?.value?.reportingYear) {
    return ['reporting-year-table-cell'];
  } else if (year == yearsForDisplay?.value?.allYearsSorted[0]) {
    return ['first-column-border'];
  }
  return [];
}
</script>

<style scoped lang="scss">
@import 'src/assets/scss/variables.scss';
.badge-light-gray {
  color: $aquamarine-dark;
  background: $paper-white;

  .material-icons-outlined {
    font-size: 1rem;
  }
}

.badge-dark-blue {
  background: $aquamarine-dark;
  color: $paper-white;
}

:deep(.first-column-border) {
  border-left: 1px solid $gray-light;
}

:deep(.reporting-year-table-cell) {
  border-left: 2px solid $aquamarine-dark;
  border-right: 2px solid $aquamarine-dark;
}
</style>

<template>
  <DataTable :value="tableData" v-if="yearsForDisplay">
    <ColumnGroup type="header">
      <Row>
        <Column header="KPI" :rowspan="2" />
        <Column
          :colspan="yearsForDisplay.numberOfHistoricalYears"
          v-if="yearsForDisplay.numberOfHistoricalYears > 0"
          class="first-column-border"
        >
          <template #header>
            <div class="p-badge badge-light-gray flex align-items-center">
              <em class="material-icons-outlined pr-1">menu_book</em>
              Historische Daten
            </div>
          </template>
        </Column>
        <Column :colspan="1" class="reporting-year-table-cell">
          <template #header> <div class="p-badge badge-dark-blue">Aktuelles Jahr</div></template>
        </Column>

        <Column :colspan="yearsForDisplay.numberOfPrognosisYears" v-if="yearsForDisplay.numberOfPrognosisYears > 0">
          <template #header>
            <div class="p-badge badge-light-gray flex align-items-center">
              <em class="material-icons-outlined pr-1">lightbulb</em> Prognosen
            </div>
          </template>
        </Column>
      </Row>
      <Row>
        <Column
          v-for="year in yearsForDisplay.allYearsSorted"
          :key="year"
          :header="year.toString()"
          :class="getClassForTableRow(year)"
        ></Column>
      </Row>
    </ColumnGroup>
    <Column field="label"></Column>
    <template v-for="year in yearsForDisplay.allYearsSorted" :key="year">
      <Column :field="year.toString()" :class="getClassForTableRow(year)"> </Column>
    </template>
  </DataTable>
</template>
