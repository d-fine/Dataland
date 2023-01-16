<template>
  <DataTable responsiveLayout="scroll" :value="dataToDisplay">
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
import { TypeOfProductionSitesNames } from "@/components/resources/frameworkDataSearch/lksg/LksgTypes";
import { listOfProductionSitesConvertedNames } from "@/components/resources/frameworkDataSearch/lksg/LksgModels";

export default defineComponent({
  inject: ["dialogRef"],
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      dataToDisplay: [] as TypeOfProductionSitesNames[],
      columns: [] as { field: string; header: string }[],
      listOfProductionSitesConvertedNames: {} as typeof listOfProductionSitesConvertedNames,
    };
  },
  mounted() {
    this.dataToDisplay = (this.dialogRef as DynamicDialogInstance).data
      .detailDataForKpi as TypeOfProductionSitesNames[];
    console.log("this.dialogRef", this.dialogRef);
    console.log("this.dataToDisplay", this.dataToDisplay);
    this.listOfProductionSitesConvertedNames = (this.dialogRef as DynamicDialogInstance).data
      .listOfProductionSitesConvertedNames as typeof listOfProductionSitesConvertedNames;
    console.log("this.listOfProductionSitesConvertedNames", this.listOfProductionSitesConvertedNames);
  },
  methods: {
    generateColsNames(): void {
      if (this.dataToDisplay.length && Array.isArray(this.dataToDisplay)) {
        for (const key of Object.keys(this.dataToDisplay[0])) {
          this.columns.push({ field: `${key}`, header: `${key}` });
        }
      }
    },
  },
  watch: {
    dataToDisplay() {
      void this.generateColsNames();
    },
  },
});
</script>
