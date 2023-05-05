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
        {{ console.log(data) }}
        <ul v-if="Array.isArray(data[col.field])">
          <li :key="el" v-for="el in data[col.field]">{{ el }}</li>
        </ul>
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
      tableType: "" as keyof typeof detailsCompanyDataTableColumnHeaders,
      columns: [] as { field: string; header: string }[],
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object>;
      tableType: keyof typeof detailsCompanyDataTableColumnHeaders;
    };
    this.listOfRowContents = dialogRefData.listOfRowContents;
    this.tableType = dialogRefData.tableType;
    console.log(this.listOfRowContents, this.columnHeaders, this.tableType);
  },
  methods: {
    /**
     * Gets the keys from a production site type to define the columns that the displayed table in this vue component
     * should have.
     */
    generateColsNames(): void {
      if (this.listOfRowContents.length && Array.isArray(this.listOfRowContents)) {
        if (typeof this.listOfRowContents[0] === "object") {
          const presentKeys = this.listOfRowContents.reduce(function (keyList: string[], rowContent) {
            for (const key of Object.keys(rowContent)) {
              if (keyList.indexOf(key) === -1) keyList.push(key);
            }
            return keyList;
          }, []);
          for (const key of presentKeys) {
            this.columns.push({ field: `${key}`, header: `${key}` });
          }
        } else {
          this.columns.push({ field: `${this.tableType}`, header: `${this.tableType}` });
        }
      }
      console.log(this.columns);
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
        return humanizeString(value);
      }
      return value;
    },
  },
  watch: {
    listOfRowContents() {
      this.generateColsNames();
    },
  },
});
</script>
