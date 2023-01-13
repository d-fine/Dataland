<template>
  <DataTable responsiveLayout="scroll" :value="displayedData">
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
      displayedData: [],
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
    this.displayedData = this.detailDataForKpi;
  },
  methods: {
    generateColsNames(): void {
      if (this.displayedData.length && Array.isArray(this.displayedData)) {
        for (const key of Object.keys(this.displayedData[0])) {
          this.columns.push({ field: `${key}`, header: `${key}` });
        }
      }
    },
  },
  watch: {
    displayedData() {
      void this.generateColsNames();
    },
  },
});
</script>
