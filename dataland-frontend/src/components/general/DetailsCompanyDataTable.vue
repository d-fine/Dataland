<template>
  <DataTable :value="listOfRowContents">
    <Column
      v-for="keyOfColumn of keysOfValuesForColumnDisplay"
      :field="keyOfColumn"
      :key="keyOfColumn"
      :header="
        columnHeaders![kpiKeyOfTable] ? columnHeaders![kpiKeyOfTable][keyOfColumn] : humanizeString(kpiKeyOfTable)
      "
      headerStyle="width: 15vw;"
    >
      <template #body="{ data }">
        <template v-if="data[keyOfColumn]">
          <ul v-if="Array.isArray(data[keyOfColumn])">
            <li :key="el" v-for="el in data[keyOfColumn]">{{ el }}</li>
          </ul>
          <div v-else-if="data[keyOfColumn].quality">
            <DataPointDataTableInModal :dataPointDisplay="data[keyOfColumn]" />
          </div>
          <div v-else-if="typeof data[keyOfColumn] === 'object'">
            <p :key="key" v-for="[key, value] in Object.entries(data[keyOfColumn])" style="margin: 0; padding: 0">
              {{ value }}
            </p>
          </div>
          <span v-else>{{ humanizeStringIfNecessary(keyOfColumn, data[keyOfColumn]) }}</span>
        </template>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import DataPointDataTableInModal from '@/components/general/DataPointDataTableInModal.vue';

export default defineComponent({
  inject: ['dialogRef'],
  name: 'DetailsCompanyDataTable',
  components: { DataPointDataTableInModal, DataTable, Column },
  data() {
    return {
      listOfRowContents: [] as Array<object | string>,
      kpiKeyOfTable: '' as string,
      keysOfValuesForColumnDisplay: [] as string[],
      keysWithValuesToBeHumanized: ['isInHouseProductionOrIsContractProcessing', 'sectors'] as string[],
      humanizeString: humanizeStringOrNumber,
      columnHeaders: {},
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      kpiKeyOfTable: string;
      columnHeaders: object;
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    this.columnHeaders = dialogRefData.columnHeaders;
    if (typeof dialogRefData.listOfRowContents[0] === 'string') {
      this.keysOfValuesForColumnDisplay.push(this.kpiKeyOfTable);
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents;
      this.generateColsNames();
    }
  },
  methods: {
    /**
     * Gets the keys from a production site type to define the columns that the displayed table in this vue component
     * should have.
     */
    generateColsNames(): void {
      const presentKeys = this.listOfRowContents.reduce(function (keyList: string[], rowContent) {
        for (const key of Object.keys(rowContent)) {
          if (keyList.indexOf(key) === -1) {
            keyList.push(key);
          }
        }
        return keyList;
      }, []);
      for (const key of presentKeys) {
        this.keysOfValuesForColumnDisplay.push(key);
      }
    },
    /**
     * Humanizes a string if the corresponding key is listed as to be humanized
     * @param key decides if the value is to be humanized
     * @param value string to be possibly humanized
     * @returns a humanized input of the value parameter if the k
     */
    humanizeStringIfNecessary(key: string, value: string): string {
      if (this.keysWithValuesToBeHumanized.includes(key)) {
        return humanizeStringOrNumber(value);
      }
      return value;
    },
  },
});
</script>
