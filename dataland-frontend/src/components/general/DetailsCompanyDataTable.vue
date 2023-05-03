<template>
  <DataTable responsiveLayout="scroll" :value="listOfProductionSitesNames">
    <Column
      v-for="col of columns"
      :field="col.field"
      :header="listOfProductionSitesConvertedNames[col.header]"
      :key="col.field"
      headerStyle="width: 15vw;"
    >
      <template #body="{ data }">
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
import {
  TypeOfProductionSitesConvertedNames,
  TypeOfProductionSitesNames,
} from "@/components/resources/frameworkDataSearch/DataModelsTypes";
import { humanizeString } from "@/utils/StringHumanizer";

export default defineComponent({
  inject: ["dialogRef"],
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      listOfProductionSitesNames: [] as TypeOfProductionSitesNames[],
      listOfProductionSitesConvertedNames: {} as TypeOfProductionSitesConvertedNames,
      columns: [] as { field: string; header: string }[],
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfProductionSitesNames: TypeOfProductionSitesNames[];
      listOfProductionSitesConvertedNames: TypeOfProductionSitesConvertedNames;
    };
    this.listOfProductionSitesNames = dialogRefData.listOfProductionSitesNames;
    this.listOfProductionSitesConvertedNames = dialogRefData.listOfProductionSitesConvertedNames;
  },
  methods: {
    /**
     * Gets the keys from a production site type to define the columns that the displayed table in this vue component
     * should have.
     */
    generateColsNames(): void {
      if (this.listOfProductionSitesNames.length && Array.isArray(this.listOfProductionSitesNames)) {
        const presentKeys = this.listOfProductionSitesNames.reduce(function (keyList: string[], productionSite) {
          for (const key of Object.keys(productionSite)) {
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
     *
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
    listOfProductionSitesNames() {
      this.generateColsNames();
    },
  },
});
</script>
