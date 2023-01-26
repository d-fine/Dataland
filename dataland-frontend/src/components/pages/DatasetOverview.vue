<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TabView class="col-12" v-model:activeIndex="activeTabIndex" @tab-change="handleTabChange">
      <TabPanel header="AVAILABLE DATASETS"> </TabPanel>
      <TabPanel header="MY DATASETS">
        <TheContent class="p-3 min-h-screen paper-section relative">
          <div class="col-12 flex flex-row justify-content-between align-items-end">
            <div
              id="dataset-summary"
              class="col-6 surface-card shadow-1 p-3 border-round-sm border-round flex flex-column"
            >
              <div class="flex flex-row justify-content-between">
                <div class="text-left font-semibold">Overview</div>
                <div class="text-right">
                  <!--TODO? link (where does it lead?)-->
                  <router-link to="/account" class="text-primary no-underline font-semibold">
                    <span>Account balance</span><span class="ml-3">></span>
                  </router-link>
                </div>
              </div>
              <div class="mt-2 flex justify-content-between">
                <div>
                  <span>Approved datasets: </span>
                  <span class="p-badge badge-green">{{ this.numApproved }}</span>
                </div>
                <div>
                  <span>Pending datasets: </span>
                  <span class="p-badge badge-orange">{{ this.numPending }}</span>
                </div>
                <div>
                  <span>Rejected datasets: </span>
                  <span class="p-badge badge-red">{{ this.numRejected }}</span>
                </div>
              </div>
            </div>
            <PrimeButton
              class="uppercase p-button p-button-sm d-letters mr-3"
              label="New Dataset"
              icon="pi pi-plus"
              @click="void this.$router.push('/companies/choose')"
            />
          </div>
          <DatasetOverviewTable :dataset-table-infos="datasetTableInfos" />
        </TheContent>
      </TabPanel>
    </TabView>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import PrimeButton from "primevue/button";
import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";
import { StoredCompany } from "@clients/backend";
import { DatasetStatus, DatasetTableInfo } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { convertUnixTimeInMsToDateString } from "@/utils/DateFormatUtils";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import TabView from "primevue/tabview";
import TabPanel from "primevue/tabpanel";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    DatalandFooter,
    PrimeButton,
    DatasetOverviewTable,
    TabView,
    TabPanel,
  },
  data() {
    return {
      datasetTableInfos: [] as DatasetTableInfo[],
      numApproved: 0,
      numPending: 0,
      numRejected: 0,
      activeTabIndex: 1,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.requestDataMetaDataForCurrentUser();
  },
  watch: {
    datasetTableInfos() {
      this.numApproved = this.countDatasetStatus(DatasetStatus.Approved);
      this.numPending = this.countDatasetStatus(DatasetStatus.Pending);
      this.numRejected = this.countDatasetStatus(DatasetStatus.Rejected);
    },
  },
  methods: {
    requestDataMetaDataForCurrentUser: async function (): Promise<void> {
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getCompanyDataControllerApi();
      const companiesMetaInfos = (
        await companyDataControllerApi.getCompanies(
          undefined,
          new Set(ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS),
          undefined,
          undefined,
          undefined,
          true
        )
      ).data;
      const userId = (await assertDefined(this.getKeycloakPromise)()).idTokenParsed!.sub;
      this.datasetTableInfos = companiesMetaInfos.flatMap((company: StoredCompany) =>
        company.dataRegisteredByDataland
          .filter(
            (dataMetaInfo) =>
              dataMetaInfo.uploaderUserId == userId &&
              ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.includes(dataMetaInfo.dataType)
          )
          .map(
            (dataMetaInfo) =>
              new DatasetTableInfo(
                company.companyInformation.companyName,
                dataMetaInfo.dataType,
                2023,
                DatasetStatus.Approved,
                convertUnixTimeInMsToDateString(dataMetaInfo.uploadTime),
                company.companyId
              )
          )
      );
    },
    countDatasetStatus(status: DatasetStatus): number {
      return this.datasetTableInfos.filter((info) => info.status.text === status.text).length;
    },
    handleTabChange(): void {
      if (this.activeTabIndex == 0) {
        void this.$router.push("/companies");
      }
    },
  },
});
</script>
