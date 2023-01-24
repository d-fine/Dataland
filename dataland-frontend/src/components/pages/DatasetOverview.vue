<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 pt-0 min-h-screen paper-section relative">
      <!-- TODO? weird title bar with tabs -->
      <div class="col-12">
        <div class="col-6 surface-card shadow-1 p-3 border-round-sm border-round flex flex-column">
          <div class="flex flex-row justify-content-between">
            <div class="text-left font-semibold">Overview</div>
            <div class="text-right">
              <!--TODO set link-->
              <router-link to="/account" class="text-primary no-underline font-semibold">
                <span>Account balance</span>
                <span class="ml-3">></span>
              </router-link>
            </div>
            <!--TODO? link (where does it lead?)-->
          </div>
          <div class="mt-2 flex justify-content-between">
            <!--TODO get actual numbers-->
            <div>
              <span>Approved datasets: </span>
              <span class="p-badge badge-green">23</span>
            </div>
            <div>
              <span>Pending datasets: </span>
              <span class="p-badge badge-orange">5</span>
            </div>
            <div>
              <span>Rejected datasets: </span>
              <span class="p-badge badge-red">4</span>
            </div>
          </div>
        </div>
        <!-- TODO NEW DATASET button -->
      </div>
      <div class="col-12 text-left">
        <DataTable
        :value="datasets"
        responsive-layout="scroll"
        id="dataset-overview-table"
        class="table-cursor mt-1"
        >
          <Column field="companyName" header="COMPANY" :sortable="true" ></Column>
          <Column :field="dataType" header="DATA FRAMEWORK" :sortable="true">
            <template #body="{ data }">
              {{ humanizeString(data.dataType) }}
            </template>
          </Column>
          <Column field="year" header="YEAR" :sortable="true"></Column>
          <Column field="status" header="STATUS" :sortable="true">
            <template #body="{ data }">
              <span :class="`p-badge badge-${data.status.color} m-0`">{{ data.status.text }}</span>
            </template>
          </Column>
          <Column field="submissionDate" header="SUBMISSION DATE" :sortable="true"></Column>
          <Column field="companyName" header="" class="d-bg-white d-datatable-column-right">
            <template #header>
              I'M A SEARCHBAR
              <!-- TODO? SEARCHBAR -->
            </template>
            <template #body="{ data }">
              <router-link :to="getRouterLinkTargetFramework(data)" class="text-primary no-underline font-bold">
                <div class="text-right">
                  <span>{{ isDatasetRejected(data) ? "ADD NEW DATASET" : "VIEW" }}</span>
                  <span class="ml-3">></span>
                </div>
              </router-link>
            </template>
          </Column>
        </DataTable>
      </div>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<!-- TODO does not apply when scoped. why? -->
<style>

#dataset-overview-table tr:hover {
  cursor: pointer;
}

#dataset-overview-table th {
  /*background-color: green;*/
}

</style>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import { defineComponent } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import {DataTypeEnum} from "@clients/backend";
import {humanizeString} from "@/utils/StringHumanizer";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    MarginWrapper,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    DatalandFooter,
    DataTable,
    Column,
  },
  data() {
    return {
      datasets: [] as DatasetTableEntry[],
      humanizeString: humanizeString,
    };
  },
  mounted() {
    this.datasets = [new DatasetTableEntry("Adidas", DataTypeEnum.EutaxonomyFinancials, 2021, DatasetStatus.Requested, "03.01.2021 06:01 PM"),
      new DatasetTableEntry("Adidas", DataTypeEnum.EutaxonomyFinancials, 2020, DatasetStatus.Approved, "03.01.2021 06:01 PM")];
  },
  methods: {
    getRouterLinkTargetFramework(datasetTableEntry: DatasetTableEntry): string {
      const framework = datasetTableEntry.dataType;
      const companyId = ""; // TODO get companyId
      if (this.isDatasetRejected(datasetTableEntry)) {
        // TODO replace with real url which does not exist yet
        return `/datasets/upload?company=${companyId}&framework=${framework}&year=${datasetTableEntry.year}`;
      } else {
        const dataId = ""; // TODO get dataId
        return `/companies/${companyId}/frameworks/${dataId}`;
      }
    },
    isDatasetRejected(datasetTableEntry: DatasetTableEntry): boolean {
      return (datasetTableEntry.status.text === DatasetStatus.Requested.text);
    },
  }
});

class DatasetTableEntry {
  constructor(readonly companyName: string,
              readonly dataType: string,
              readonly year: number,
              readonly status: DatasetStatus,
              readonly submissionDate: string) {}
}

class DatasetStatus {
  static readonly Approved = new DatasetStatus("APPROVED", "green");
  static readonly Requested = new DatasetStatus("REQUESTED", "cyan");
  static readonly Pending = new DatasetStatus("PENDING", "orange");
  static readonly Rejected = new DatasetStatus("REJECTED", "red");
  static readonly Draft = new DatasetStatus("DRAFT", "gray");

  private constructor(readonly text: string, readonly color: string) {}
}

</script>
