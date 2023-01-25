<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 pt-0 min-h-screen paper-section relative">
      <!-- TODO? weird title bar with tabs -->
      <div class="col-12 flex flex-row justify-content-between align-items-end">
        <div id="dataset-summary" class="col-6 surface-card shadow-1 p-3 border-round-sm border-round flex flex-column">
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
        <PrimeButton
          class="uppercase p-button p-button-sm d-letters mr-3"
          label="New Dataset"
          icon="pi pi-plus"
          @click="void this.$router.push('/companies/choose')"
        />
      </div>
      <DatasetOverviewTable :dataset-table-infos="datasetTableInfos" />
    </TheContent>
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
import {dateFormatOptions, formatDate} from "@/utils/DateFormatUtils";
import {ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS} from "@/utils/Constants";

export default defineComponent({
  name: "DatasetOverview",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    DatalandFooter,
    PrimeButton,
    DatasetOverviewTable,
  },
  data() {
    return {
      datasetTableInfos: [] as DatasetTableInfo[],
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
  methods: {
    async requestDataMetaDataForCurrentUser(): Promise<void> {
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getCompanyDataControllerApi();
      const companiesMetaInfos = (
        await companyDataControllerApi.getCompanies(undefined, new Set(ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS), undefined, undefined, undefined, true)
      ).data;
      this.datasetTableInfos = companiesMetaInfos.flatMap((company: StoredCompany) =>
        company.dataRegisteredByDataland.map(
          (dataMetaInfo) =>
            new DatasetTableInfo(
              company.companyInformation.companyName,
              dataMetaInfo.dataType,
              2023,
              DatasetStatus.Approved,
              formatDate(dataMetaInfo.uploadTime, dateFormatOptions),
              company.companyId
            )
        )
      );
    },
  },
});
</script>
