<template>
  <div v-if="companyInformation" class="grid align-items-end text-left">
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
import { CompanyInformation } from "@clients/backend";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "CompanyInformation",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      companyInformation: null as CompanyInformation | null,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },
  created() {
    void this.getCompanyInformation();
  },
  watch: {
    async companyID() {
      await this.getCompanyInformation();
    },
  },
  methods: {
    async getCompanyInformation() {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        const response = await companyDataControllerApi.getCompanyById(this.companyID as string);
        this.companyInformation = response.data.companyInformation;
      } catch (error) {
        console.error(error);
        this.companyInformation = null;
      }
    },
    orderOfMagnitudeSuffix(value: number): string {
      return convertCurrencyNumbersToNotationWithLetters(value);
    },
  },
});
</script>
