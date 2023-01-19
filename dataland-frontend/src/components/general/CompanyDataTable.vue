<template>
  <DynamicDialog />
  <div class="col-12 text-left">
    <h2>{{ tableDataTitle }}</h2>
  </div>
  <div>
    <div class="card">
      <DataTable
        :value="dataToDisplay"
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
            <span class="col-10">{{
              kpisNames[slotProps.data.kpi] ? kpisNames[slotProps.data.kpi] : slotProps.data.kpi
            }}</span>
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
        <Column v-for="col of dataSetColumns" :field="col" :header="col.split('-')[0]" :key="col">
          <template #body="{ data }">
            <a
              v-if="Array.isArray(data[col]) && data[col].length"
              @click="
                () => {
                  openModalAndDisplayArrayOfKpiValues(data[col], kpisNames[data.kpi]);
                }
              "
              class="link"
              >Show "{{ kpisNames[data.kpi] }}"
              <em class="material-icons" aria-hidden="true" title=""> dataset </em>
            </a>
            <span v-else>{{ Array.isArray(data[col]) ? "" : data[col] }}</span>
          </template>
        </Column>

        <Column field="group" header="Impact Area"></Column>
        <template #groupheader="slotProps">
          <span>{{ slotProps.data.group ? impactTopicNames[slotProps.data.group] : "" }}</span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Tooltip from "primevue/tooltip";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { listOfProductionSitesConvertedNames } from "@/components/resources/frameworkDataSearch/DataModelsTranslations";
import DynamicDialog from "primevue/dynamicdialog";

export default defineComponent({
  name: "CompanyDataTable",
  components: { DataTable, Column, DynamicDialog },
  directives: {
    tooltip: Tooltip,
  },
  data() {
    return {
      dataToDisplay: [],
      expandedRowGroups: ["_general"],
      listOfProductionSitesConvertedNames,
    };
  },
  props: {
    dataSet: {
      type: Array,
      default: () => [],
    },
    dataSetColumns: {
      type: Array,
      default: () => [],
    },
    kpisNames: {
      type: Object,
      default: () => ({}),
    },
    hintsForKpis: {
      type: Object,
      default: () => ({}),
    },
    impactTopicNames: {
      type: Object,
      default: () => ({}),
    },
    tableDataTitle: {
      type: String,
      default: "",
    },
  },
  mounted() {
    this.dataToDisplay = this.dataSet;
  },
  methods: {
    openModalAndDisplayArrayOfKpiValues(onShowData: [], onShowDataTitle: string) {
      this.$dialog.open(DetailsCompanyDataTable, {
        props: {
          header: onShowDataTitle,
          modal: true,
        },
        data: {
          detailDataForKpi: onShowData,
          listOfProductionSitesConvertedNames: listOfProductionSitesConvertedNames,
        },
      });
    },
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
