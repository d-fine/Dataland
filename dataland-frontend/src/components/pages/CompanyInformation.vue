<template>
  <TheContent>
    <div v-if="waitingForData" class="inline-loading meta-data-height text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </div>
    <div v-if="companyInformation && !waitingForData" class="meta-data-height grid align-items-end text-left">
      <div class="col-12">
        <h1 class="mb-0">{{ companyInformation.companyName }}</h1>
      </div>

      <div class="col-4">
        <span>Headquarter: </span>
        <span class="font-semibold">{{ companyInformation.headquarters }}</span>
      </div>
      <div class="col-4">
        <span>Sector: </span>
        <span class="font-semibold">{{ companyInformation.sector }}</span>
      </div>
    </div>
  </TheContent>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject } from "vue";
import { CompanyInformation } from "@clients/backend";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import TheContent from "@/components/generics/TheContent.vue";

export default defineComponent({
  name: "CompanyInformation",
  components: { TheContent },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      companyInformation: null as CompanyInformation | null,
      waitingForData: true,
    };
  },
  props: {
    companyID: {
      type: String,
      default: "loading",
    },
  },
  mounted() {
    void this.getCompanyInformation();
  },
  watch: {
    companyID() {
      void this.getCompanyInformation();
    },
  },
  methods: {
    /**
     * Uses the dataland API to retrieve information about the company identified by the local
     * companyId object.
     */
    async getCompanyInformation() {
      try {
        this.waitingForData = true;
        if (this.companyID != "loading") {
          const companyDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getCompanyDataControllerApi();
          const response = await companyDataControllerApi.getCompanyById(this.companyID);
          this.companyInformation = response.data.companyInformation;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
        this.companyInformation = null;
      }
    },
  },
});
</script>

<style scoped>
.inline-loading {
  width: 450px;
}
.meta-data-height {
  height: 110px;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  margin-bottom: 0;
}
</style>
