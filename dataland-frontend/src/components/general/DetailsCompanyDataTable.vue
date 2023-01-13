<template>
  <DataTable responsiveLayout="scroll" :value="dataToDisplay">
    <Column
      v-for="col of columns"
      :field="col.field"
      :header="listOfProductionSitesNames[col.header]"
      :key="col.field"
      headerStyle="min-width: 15vw;"
    >
      <template #body="{ data }">
        <span>{{ data[col.field] }}</span>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default defineComponent({
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      dataToDisplay: [],
      columns: [],
    };
  },
  props: {
    detailDataForKpi: {
      type: Array,
      default: [],
    },
    listOfProductionSitesNames: {
      type: Object,
      default: {},
    },
  },
  mounted() {
    this.dataToDisplay = this.detailDataForKpi;
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
