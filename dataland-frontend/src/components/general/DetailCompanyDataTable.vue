<template>
  <div>
    <div class="card">
      <h5>Lksg data</h5>
      <DataTable
        :value="customers"
        rowGroupMode="subheader"
        groupRowsBy="group"
        dataKey="group"
        responsiveLayout="scroll"
        :expandableRowGroups="true"
        :reorderableColumns="true"
        v-model:expandedRowGroups="expandedRowGroups"
        >
        <Column
          bodyClass="headers-bg"
          headerStyle="min-width: 30vw;"
          headerClass="horizontal-headers-size"
          field="kpi"
          header="KPIs"
        >
          <template #body="slotProps">
            <div>
              {{ LksgKpis[slotProps.data.kpi] }}
              <img src="https://www.primefaces.org/wp-content/uploads/2020/05/placeholder.png" width="32" style="vertical-align: middle; float:right;" />
            </div>
          </template>
        </Column>
        <Column v-for="col of dataSetColumns" :field="col" :header="col.split('-')[0]" :key="col"></Column>
        <Column field="group" header="Impact Area"></Column>
        <template #groupheader="slotProps">
          <span>{{ slotProps.data.group }}</span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

import { LksgKpis } from "@/components/resources/frameworkDataSearch/lksg/LksgModels";

export default defineComponent({
  name: "DetailCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      LksgKpis,
      customers: null,
      expandedRowGroups: ['General'],
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
  },
  created() {
    this.customers = this.dataSet;
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
