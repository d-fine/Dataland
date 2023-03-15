<template>
  <TheContent>
    <div v-if="waitingForData" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </div>
    <div v-else-if="companyInformation && !waitingForData" class="grid align-items-end text-left">
      <div class="col-12">
        <h1 class="mb-0" data-test="companyNameTitle">{{ companyInformation.companyName }}</h1>
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
    <div v-else-if="companyIdDoesNotExist" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
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
      companyIdDoesNotExist: false,
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
        if (this.getErrorMessage(error).includes("404")) {
          this.companyIdDoesNotExist = true;
        }
        this.waitingForData = false;
        this.companyInformation = null;
      }
    },
    /**
     * Tries to find a message in an error
     *
     * @param error the error to extract a message from
     * @returns the extracted message
     */
    getErrorMessage(error: unknown) {
      const noStringMessage = error instanceof Error ? error.message : "";
      return typeof error === "string" ? error : noStringMessage;
    },
  },
});
</script>

<style scoped>
.inline-loading {
  width: 450px;
}
</style>
