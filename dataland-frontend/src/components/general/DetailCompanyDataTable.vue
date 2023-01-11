<template>
  <div>
    <div class="card">
      <h5>LKSG data</h5>
      <DataTable
        :value="customers"
        rowGroupMode="subheader"
        groupRowsBy="group"
        dataKey="group"
        sortField="group"
        :sortOrder="1"
        sortMode="single"
        responsiveLayout="scroll"
        :expandableRowGroups="true"
        :reorderableColumns="true"
        v-model:expandedRowGroups="expandedRowGroups"
      >
        <Column
          bodyClass="headers-bg flex"
          headerStyle="width: 30vw;"
          headerClass="horizontal-headers-size"
          field="kpi"
          header="KPIs"
        >
          <template #body="slotProps">
            <span class="col-10">{{ kpisNames[slotProps.data.kpi] ? kpisNames[slotProps.data.kpi] : "" }}</span>

            <em
              class="material-icons info-icon col-2"
              aria-hidden="true"
              title="kpisNames[slotProps.data.kpi] ? kpisNames[slotProps.data.kpi] : ''"
              v-tooltip.top="{
                value: hintsForKpis[slotProps.data.kpi] ? hintsForKpis[slotProps.data.kpi] : '',
              }"
              >info</em
            >
          </template>
        </Column>
        <Column v-for="col of dataSetColumns" :field="col" :header="col.split('-')[0]" :key="col"></Column>
        <Column field="group" header="Impact Area"></Column>
        <template #groupheader="slotProps">
          <span>{{ slotProps.data.group ? slotProps.data.group : "" }}</span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import Tooltip from "primevue/tooltip";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default defineComponent({
  name: "DetailCompanyDataTable",
  components: { DataTable, Column },
  directives: {
    tooltip: Tooltip,
  },
  data() {
    return {
      customers: null,
      expandedRowGroups: ["General"],
    };
  },
  props: {
    dataSet: {
      type: [],
      default: [],
    },
    dataSetColumns: {
      type: [],
      default: [],
    },
    kpisNames: {
      type: Object,
      default: {},
    },
    hintsForKpis: {
      type: Object,
      default: {},
    },
  },
  created() {
    this.customers = this.dataSet;
    console.log("this.customers", this.kpisNames);
  },
});
</script>

<style lang="scss" scoped>
.p-rowgroup-footer td {
  font-weight: 500;
}
.horizontal-headers-size {
  width: 500px;
}
::v-deep(.p-rowgroup-header) {
  span {
    font-weight: 500;
  }
  .p-row-toggler {
    vertical-align: middle;
    margin-right: 0.25rem;
    float: right;
    cursor: pointer;
  }
}
</style>
