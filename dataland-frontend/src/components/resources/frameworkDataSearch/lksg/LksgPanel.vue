<template>
  <div  class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Data..</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
    {{ dataSet }}
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { LksgData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "LksgPanel",
  components: { },
  data() {
    return {
      dataSet: null as LksgData | null | undefined,
    };
  },
  props: {
    dataID: {
      type: String,
      default: "loading",
    },
  },
  mounted() {
    void this.getCompanyLksgDataset();
  },
  watch: {
    dataID() {
      void this.getCompanyLksgDataset();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      waitingForData: true,
    };
  },
  methods: {
    async getCompanyLksgDataset() {
      try {
        this.waitingForData = true;
        if (this.dataID != "loading") {
          const LksgDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getLksgDataControllerApi();
          const companyAssociatedData =
            await LksgDataControllerApi.getCompanyAssociatedLksgData(
            assertDefined(this.dataID)
          );
          this.dataSet = companyAssociatedData.data.data;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
      }
    },
    getSectionHeading(type: string): string {
      const mapping: { [key: string]: string } = {
        CreditInstitution: "Credit Institution",
        AssetManagement: "Asset Management",
        InsuranceOrReinsurance: "Insurance and Reinsurance",
        InvestmentFirm: "Investment Firm",
      };
      return mapping[type];
    },
  },
});
</script>
