<template>
  <div>
    <div class="card">
      <h5>Lksg data</h5>
      <DataTable
        :value="customers"
        rowGroupMode="subheader"
        groupRowsBy="group"
        responsiveLayout="scroll"
        :expandableRowGroups="true"
        v-model:expandedRowGroups="expandedRowGroups"
      ><Column bodyClass="headers-bg" headerStyle="width: 30vw;" headerClass="horizontal-headers-size" field="kpi" header="KPIs">
        <template #body="slotProps">
          <div>{{ LksgKpis[slotProps.data.kpi] }}</div>

        </template>
      </Column>
        <Column v-for="col of dataSetColumns" :field="col" :header="col.split('-')[0]" :key="col"></Column>
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

import { LksgKpis } from "@/components/resources/frameworkDataSearch/lksg/LksgModels"

export default defineComponent({
  name: "DetailCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      LksgKpis,
      customers: null,
      expandedRowGroups: [],
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
  mounted() {
    this.customers = this.dataSet;
  },
});
</script>

<style lang="scss" scoped>
  .p-rowgroup-footer td {
    font-weight: 700;
  }
  .horizontal-headers-size {
    width: 500px;
  }
  ::v-deep(.p-rowgroup-header) {
    span {
      font-weight: 700;
    }

    .p-row-toggler {
      vertical-align: middle;
      margin-right: 0.25rem;
      float: right;
      cursor: pointer;
    }
}
</style>
