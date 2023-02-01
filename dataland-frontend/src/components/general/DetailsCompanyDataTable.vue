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
        <span v-else>{{ data[col.field] }}</span>
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
  TypeOfProductionSitesNames,
  TypeOfProductionSitesConvertedNames,
} from "@/components/resources/frameworkDataSearch/DataModelsTypes";

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
        for (const key of Object.keys(this.listOfProductionSitesNames[0])) {
          this.columns.push({ field: `${key}`, header: `${key}` });
        }
      }
    },
  },
  watch: {
    listOfProductionSitesNames() {
      this.generateColsNames();
    },
  },
});
</script>
