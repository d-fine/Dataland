<template>
  <div v-if="getCompanyResponse" class="grid align-items-end text-left">
    <div class="col-12">
      <h1 class="mb-0">{{ companyInformation.companyName }}</h1>
    </div>

    <div class="col-4">
      <span>Market Cap:</span>
      <span class="font-semibold">€ {{ orderOfMagnitudeSuffix(companyInformation.marketCap) }}</span>
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

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";

export default {
  name: "CompanyInformation",
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
  created() {
    this.getCompanyInformation();
  },
  watch: {
    companyID() {
      this.getCompanyInformation();
    },
  },
  inject: ["getKeycloakPromise"],
  methods: {
    async getCompanyInformation() {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getCompanyDataControllerApi();
        this.getCompanyResponse = await companyDataControllerApi.getCompanyById(this.companyID);
        this.companyInformation = this.getCompanyResponse.data.companyInformation;
      } catch (error) {
        console.error(error);
        this.getCompanyResponse = null;
      }
    },
    orderOfMagnitudeSuffix(value) {
      return convertCurrencyNumbersToNotationWithLetters(value);
    },
  },
};
</script>
