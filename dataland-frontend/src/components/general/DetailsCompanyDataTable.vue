<template>
  <DataTable responsiveLayout="scroll" :value="listOfRowContents">
    <Column
      v-for="col of columns"
      :field="col.field"
      :header="columnHeaders[tableType][col.header]"
      :key="col.field"
      headerStyle="width: 15vw;"
    >
      <template #body="{ data }">
        <ul v-if="Array.isArray(data[col.field])">
          <li :key="el" v-for="el in data[col.field]">{{ el }}</li>
        </ul>
        <div v-else-if="typeof data[col.field] === 'object'">
          <p :key="key" v-for="[key, value] in Object.entries(data[col.field])">
            {{ value }}
          </p>
        </div>
        <span v-else>{{ humanizeStringIfNeccessary(col.field, data[col.field]) }}</span>
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
      tableType: "" as string,
      columns: [] as { field: string; header: string }[],
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      tableType: string;
    };
    this.tableType = dialogRefData.tableType;
    if (typeof dialogRefData.listOfRowContents[0] === "string") {
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.tableType]: o }));
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
      if (this.listOfRowContents.length && Array.isArray(this.listOfRowContents)) {
        const presentKeys = this.listOfRowContents.reduce(function (keyList: string[], rowContent) {
          for (const key of Object.keys(rowContent)) {
            if (keyList.indexOf(key) === -1) keyList.push(key);
          }
          return keyList;
        }, []);
        for (const key of presentKeys) {
          this.columns.push({ field: `${key}`, header: `${key}` });
        }
      }
    },
    /**
     * Humanizes a string if the corresponding key is listed as to be humanized
     * @param key decides if the value is to be humanized
     * @param value string to be possibly humanized
     * @returns a humanized input of the value parameter if the k
     */
    humanizeStringIfNeccessary(key: string, value: string): string {
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
