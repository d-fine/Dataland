<template>
  <DataTable
      responsiveLayout="scroll"
      :value="displayedData"
  >
    <Column
        v-for="col of columns"
        :field="col.field"
        :header="col.header"
        :key="col.field"
    >
      <template #body="{data}">

        <DetailsCompanyDataTable v-if="Array.isArray(data[col])" :detailDataForKpi="data[col]" />
        <span v-else>{{data[col]}}</span>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">

import {defineComponent} from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default defineComponent({
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      displayedData: [],
      columns: [
        {field: 'name', header: 'name'},
        {field: 'isInHouseProductionOrIsContractProcessing', header: 'isInHouseProductionOrIsContractProcessing'},
        {field: 'address', header: 'address'},
        {field: 'listOfGoodsAndServices', header: 'listOfGoodsAndServices'}
      ],
    };
  },
  props: {
    detailDataForKpi: {
      type: [],
      default: [],
    },
  },
  mounted() {
    this.displayedData = this.detailDataForKpi;
  },
  methods: {
    generateColsNames(): void {

      if (this.displayedData.length && Array.isArray(this.displayedData)) {

        for (const key of Object.keys(this.displayedData[0])) {
          console.log('KEYY ----->', key)
          this.columns.push(
              {field: `${key}`, header: `${key}`},
          );
        }
      }
    },
  },
  watch: {
    // displayedData() {
    //   console.log('this.displayedData -----> ', this.displayedData);
    //   void this.generateColsNames();
    // }
  },
});
</script>