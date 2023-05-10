<template>
  <div class="card">
    <DataTable
      :value="kpiDataObjectsToDisplay"
      rowGroupMode="subheader"
      groupRowsBy="subcategoryKey"
      dataKey="subcategoryKey"
      sortField="subcategoryKey"
      :sortOrder="1"
      sortMode="single"
      responsiveLayout="scroll"
      :expandableRowGroups="true"
      :reorderableColumns="true"
      v-model:expandedRowGroups="expandedRowGroups"
    >
      <Column
        bodyClass="headers-bg"
        headerStyle="width: 30vw;"
        headerClass="horizontal-headers-size"
        field="kpiKey"
        header="KPIs"
      >
        <template #body="slotProps">
          <span class="table-left-label" :data-test="slotProps.data.kpiKey">{{ slotProps.data.kpiLabel }}</span>
          <em
            class="material-icons info-icon"
            aria-hidden="true"
            :title="slotProps.data.kpiLabel ? slotProps.data.kpiLabel : ''"
            v-tooltip.top="{
              value: slotProps.data.kpiDescription ? slotProps.data.kpiDescription : '',
            }"
            >info</em
          >
        </template>
      </Column>
      <Column
        v-for="reportingPeriod of reportingPeriodsOfDataSets"
        headerClass="horizontal-headers-size"
        :field="reportingPeriod.dataId"
        :header="reportingPeriod.reportingPeriod"
        :key="reportingPeriod.dataId"
      >
        <template #body="{ data }">
          <a
            v-if="
              Array.isArray(data[reportingPeriod.dataId]) &&
              (data[reportingPeriod.dataId].length > 1 ||
                data[reportingPeriod.dataId].some((el) => typeof el === 'object'))
            "
            @click="openModalAndDisplayValuesInSubTable(data[reportingPeriod.dataId], data.kpiLabel, data.kpiKey)"
            class="link"
            >Show "{{ data.kpiLabel }}"
            <em class="material-icons" aria-hidden="true" title=""> dataset </em>
          </a>
          <span v-else-if="typeof data[reportingPeriod.dataId] === 'object' && data[reportingPeriod.dataId]?.value">
            {{ data[reportingPeriod.dataId].value }}
          </span>

          <span v-else
            >{{
              Array.isArray(data[reportingPeriod.dataId])
                ? data[reportingPeriod.dataId][0]
                : data[reportingPeriod.dataId]
            }}
          </span>
        </template>
      </Column>

      <Column field="subcategoryKey" header="Impact Area"></Column>
      <template #groupheader="slotProps">
        <span :data-test="slotProps.data.subcategoryKey" :id="slotProps.data.subcategoryKey" style="cursor: pointer">
          {{ slotProps.data.subcategoryLabel ? slotProps.data.subcategoryLabel : slotProps.data.subcategoryKey }}
        </span>
      </template>
    </DataTable>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import Tooltip from "primevue/tooltip";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { kpiDataObject } from "@/components/resources/frameworkDataSearch/KpiDataObject";

export default defineComponent({
  name: "LksgCompanyDataTable",
  components: { DataTable, Column },
  directives: {
    tooltip: Tooltip,
  },
  data() {
    return {
      kpiDataObjectsToDisplay: [] as kpiDataObject[],
      expandedRowGroups: ["_masterData"],
    };
  },
  props: {
    kpiDataObjects: {
      type: new Map() as unknown as PropType<Map<string, kpiDataObject>>,
      default: () => new Map(),
    },
    reportingPeriodsOfDataSets: {
      type: Array,
      default: () => [],
    },
    tableDataTitle: {
      type: String,
      default: "",
    },
  },
  mounted() {
    this.kpiDataObjectsToDisplay = Array.from(this.kpiDataObjects.values());

    document.addEventListener("click", (e) => this.expandRowGroupOnHeaderClick(e));
  },
  methods: {
    /**
     * Opens a modal to display a table with the provided list of production sites
     * @param listOfValues An array consisting of production sites
     * @param modalTitle The title for the modal, which is derived from the key of the KPI
     * @param kpiKey the key of the KPI used to determine the type of Subtable that needs to be displayed
     */
    openModalAndDisplayValuesInSubTable(listOfValues: [], modalTitle: string, kpiKey: string) {
      this.$dialog.open(DetailsCompanyDataTable, {
        props: {
          header: modalTitle,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: listOfValues,
          tableType: kpiKey,
        },
      });
    },
    /**
     * Enables groupRowExpansion (and collaps) when clicking on the whole header row
     * @param event a click event
     */
    expandRowGroupOnHeaderClick(event: Event) {
      const id = (event.target as Element).id;

      const matchingChild = Array.from((event.target as Element).children).filter((child: Element) =>
        this.kpiDataObjectsToDisplay.some((dataObject) => dataObject.subcategoryKey === child.id)
      )[0];

      if (matchingChild || this.kpiDataObjectsToDisplay.some((dataObject) => dataObject.subcategoryKey === id)) {
        const index = this.expandedRowGroups.indexOf(matchingChild?.id ?? id);
        if (index === -1) this.expandedRowGroups.push(matchingChild?.id ?? id);
        else this.expandedRowGroups.splice(index, 1);
      }
    },
  },
});
</script>

<style lang="scss" scoped>
.p-rowgroup-footer td {
  font-weight: 500;
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
