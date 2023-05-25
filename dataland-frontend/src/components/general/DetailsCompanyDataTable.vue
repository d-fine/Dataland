<template>
  <DataTable :value="listOfRowContents">
    <Column
      v-for="col of columns"
      :field="col"
      :key="col"
      :header="columnHeaders[kpiKeyOfTable] ? columnHeaders[kpiKeyOfTable][col] : undefined"
      headerStyle="width: 15vw;"
    >
      <template #body="{ data }">
        <template v-if="data[col]">
          <ul v-if="Array.isArray(data[col])">
            <li :key="el" v-for="el in data[col]">{{ el }}</li>
          </ul>
          <div v-else-if="typeof data[col] === 'object'">
            <p :key="key" v-for="[key, value] in Object.entries(data[col])" style="margin: 0; padding: 0">
              {{ value }}
            </p>
          </div>
          <span v-else>{{ humanizeStringIfNecessary(col, data[col]) }}</span>
        </template>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { humanizeString } from "@/utils/StringHumanizer";
import { detailsCompanyDataTableColumnHeaders } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  inject: ["dialogRef"],
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      listOfRowContents: [] as Array<object | string>,
      columnHeaders: detailsCompanyDataTableColumnHeaders,
      kpiKeyOfTable: "" as string,
      columns: [] as string[],
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      kpiKeyOfTable: string;
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    if (typeof dialogRefData.listOfRowContents[0] === "string") {
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents;
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
          if (keyList.indexOf(key) === -1) keyList.push(key);
        }
        return keyList;
      }, []);
      for (const key of presentKeys) {
        this.columns.push(key);
      }
    },
    /**
     * Humanizes a string if the corresponding key is listed as to be humanized
     * @param key decides if the value is to be humanized
     * @param value string to be possibly humanized
     * @returns a humanized input of the value parameter if the k
     */
    humanizeStringIfNecessary(key: string, value: string): string {
      const keysWithValuesToBeHumanized = ["isInHouseProductionOrIsContractProcessing"];
      if (keysWithValuesToBeHumanized.includes(key)) {
        return this.humanizeString(value);
      }
      return value;
    },
    humanizeString,
  },
  watch: {
    listOfRowContents() {
      this.generateColsNames();
    },
  },
});
</script>
