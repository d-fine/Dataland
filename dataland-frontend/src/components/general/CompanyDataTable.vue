<template>
  <DynamicDialog />
  <div class="col-12 text-left">
    <h2>{{ tableDataTitle }}</h2>
  </div>
  <div>
    <div class="card">
      <DataTable
        :value="kpiDataObjectsToDisplay"
        rowGroupMode="subheader"
        groupRowsBy="subAreaKey"
        dataKey="subAreaKey"
        sortField="subAreaKey"
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
            <span class="table-left-label">{{
              kpiNameMappings[slotProps.data.kpiKey] ? kpiNameMappings[slotProps.data.kpiKey] : slotProps.data.kpiKey
            }}</span>
            <em
              class="material-icons info-icon"
              aria-hidden="true"
              :title="kpiNameMappings[slotProps.data.kpiKey] ? kpiNameMappings[slotProps.data.kpiKey] : ''"
              v-tooltip.top="{
                value: kpiInfoMappings[slotProps.data.kpiKey] ? kpiInfoMappings[slotProps.data.kpiKey] : '',
              }"
              >info</em
            >
          </template>
        </Column>
        <Column
          v-for="dataDate of DataDateOfDataSets"
          :field="dataDate.dataId"
          :header="dataDate.dataDate?.split('-')[0]"
          :key="dataDate.dataId"
        >
          <template #body="{ data }">
            <a
              v-if="Array.isArray(data[dataDate.dataId]) && data[dataDate.dataId].length"
              @click="openModalAndDisplayListOfProductionSites(data[dataDate.dataId], kpiNameMappings[data.kpiKey])"
              class="link"
              >Show "{{ kpiNameMappings[data.kpiKey] }}"
              <em class="material-icons" aria-hidden="true" title=""> dataset </em>
            </a>
            <template v-else-if="typeof data[dataDate.dataId] === 'object' && data[dataDate.dataId] !== null">
              <table class="detail-table" aria-describedby="Details Table">
                <th style="display: none"></th>
                <caption style="display: none">
                  Details Table
                </caption>
                <template v-for="(value, key, index) in data[dataDate.dataId]" :key="index + key">
                  <tr v-if="typeof value === 'string'">
                    <td class="key-td">{{ kpiNameMappings[key] ? kpiNameMappings[key] : key }}</td>
                    <td class="value-td">{{ value }}</td>
                  </tr>
                  <template v-if="typeof value === 'object' && value !== null">
                    <tr>
                      <td rowspan="4" class="key-td text-center">
                        {{ kpiNameMappings[key] ? kpiNameMappings[key] : key }}
                      </td>
                    </tr>
                    <tr v-for="(value, key, index) in value" :key="index + key">
                      <td class="internal-key-td">{{ kpiNameMappings[key] ? kpiNameMappings[key] : key }}</td>
                      <td class="internal-value-td">{{ value }}</td>
                    </tr>
                  </template>
                </template>
              </table>
            </template>

            <template v-else-if="typeof data[dataDate.dataId] === 'string' && isValidHttpUrl(data[dataDate.dataId])">
              <a :href="sanitizeHref(data[dataDate.dataId])" class="link">Link</a>
            </template>

            <span v-else>{{ Array.isArray(data[dataDate.dataId]) ? "" : data[dataDate.dataId] }}</span>
          </template>
        </Column>

        <Column field="subAreaKey" header="Impact Area"></Column>
        <template #groupheader="slotProps">
          <span>{{
            subAreaNameMappings[slotProps.data.subAreaKey]
              ? subAreaNameMappings[slotProps.data.subAreaKey]
              : slotProps.data.subAreaKey
          }}</span>
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
import { sanitizeHref } from "@/utils/DataTableDisplay";
import { isValidHttpUrl } from "@/utils/TypeCheck";

export default defineComponent({
  name: "CompanyDataTable",
  components: { DataTable, Column, DynamicDialog },
  directives: {
    tooltip: Tooltip,
  },
  data() {
    return {
      sanitizeHref: sanitizeHref,
      kpiDataObjectsToDisplay: [],
      expandedRowGroups: ["_general"],
      listOfProductionSitesConvertedNames,
      isValidHttpUrl: isValidHttpUrl,
    };
  },
  props: {
    kpiDataObjects: {
      type: Array,
      default: () => [],
    },
    DataDateOfDataSets: {
      type: Array,
      default: () => [],
    },
    kpiNameMappings: {
      type: Object,
      default: () => ({}),
    },
    kpiInfoMappings: {
      type: Object,
      default: () => ({}),
    },
    subAreaNameMappings: {
      type: Object,
      default: () => ({}),
    },
    tableDataTitle: {
      type: String,
      default: "",
    },
  },
  mounted() {
    this.kpiDataObjectsToDisplay = this.kpiDataObjects;
  },
  methods: {
    /**
     * Opens a modal to display a table with the provided list of production sites
     *
     * @param listOfProductionSites An array consisting of production sites
     * @param modalTitle The title for the modal, which is derived from the key of the KPI
     */
    openModalAndDisplayListOfProductionSites(listOfProductionSites: [], modalTitle: string) {
      this.$dialog.open(DetailsCompanyDataTable, {
        props: {
          header: modalTitle,
          modal: true,
        },
        data: {
          listOfProductionSitesNames: listOfProductionSites,
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
.detail-table {
  background-color: var(--surface-0);
  tr {
    &:hover {
      background-color: transparent;
    }
    .key-td {
      background-color: var(--surface-300);
    }
    .internal-key-td {
      background-color: var(--surface-100);
    }
  }
}
</style>
