<template>
  <div>
    <Toast />


    <div class="card">
      <h5>Row Groups</h5>
      <DataTable
        :value="customers"
        rowGroupMode="subheader"
        groupRowsBy="group"
        sortMode="single"
        sortField="group"
        :sortOrder="1"
        responsiveLayout="scroll"
        :expandableRowGroups="true"
        v-model:expandedRowGroups="expandedRowGroups"
        @rowgroupExpand="onRowGroupExpand"
        @rowgroupCollapse="onRowGroupCollapse"
      >
        <Column field="dataDate" header="Data Date"></Column>
        <Column field="companyLegalForm" header="companyLegalForm"></Column>
        <Column field="totalRevenue" header="totalRevenue">
          <template #body="slotProps">
            <span class="image-text">{{ slotProps.data.totalRevenue }}</span>
          </template>
        </Column>
        <Column field="shareOfTemporaryWorkers" header="shareOfTemporaryWorkers">
          <template #body="slotProps">
            <span>{{ slotProps.data.shareOfTemporaryWorkers }}</span>
          </template>
        </Column>
        <template #groupheader="slotProps">
          <span class="image-text">{{ slotProps.data.group }}</span>
        </template>
        <template #groupfooter="slotProps">
          <td colspan="4" style="text-align: right">Total Customers</td>
        </template>
      </DataTable>
    </div>

  </div>
</template>

<script>
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default defineComponent({
  name: "DetailCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      customers: null,
      expandedRowGroups: [],
    };
  },
  props: {
    dataSet: {
      type: [],
      default: [],
    },
  },
  mounted() {
    this.customers = this.dataSet;
  },

  methods: {
    onRowGroupExpand(event) {
      this.$toast.add({ severity: "info", summary: "Row Group Expanded", detail: "Value: " + event.data, life: 3000 });
    },
    onRowGroupCollapse(event) {
      this.$toast.add({
        severity: "success",
        summary: "Row Group Collapsed",
        detail: "Value: " + event.data,
        life: 3000,
      });
    },
    calculateCustomerTotal(name) {
      let total = 0;

      if (this.customers) {
        for (const customer of this.customers) {
          if (customer.country.name === name) {
            total++;
          }
        }
      }

      return total;
    },
  },
});
</script>

<style lang="scss" scoped>
.p-rowgroup-footer td {
  font-weight: 700;
}

::v-deep(.p-rowgroup-header) {
  span {
    font-weight: 700;
  }

  .p-row-toggler {
    vertical-align: middle;
    margin-right: 0.25rem;
  }
}
</style>
