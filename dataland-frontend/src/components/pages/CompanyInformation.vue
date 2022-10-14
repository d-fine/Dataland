<template>
  <div v-if="getCompanyResponse" class="grid align-items-end text-left">
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
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "CompanyInformation",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      getCompanyResponse: null,
      companyInformation: null,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },
  async created() {
    await this.getCompanyInformation();
  },
  watch: {
    async companyID() {
      await this.getCompanyInformation();
    },
  },
  methods: {
    async getCompanyInformation() {
      try {
        if (this.getKeycloakPromise !== undefined) {
          const companyDataControllerApi = await new ApiClientProvider(
            this.getKeycloakPromise()
          ).getCompanyDataControllerApi();
          this.getCompanyResponse = await companyDataControllerApi.getCompanyById(this.companyID as string);
          this.companyInformation = this.getCompanyResponse.data.companyInformation;
        }
      } catch (error) {
        console.error(error);
        this.getCompanyResponse = null;
      }
    },
    orderOfMagnitudeSuffix(value: number): string {
      return convertCurrencyNumbersToNotationWithLetters(value);
    },
  },
});
</script>
