<template>
  <TheHeader :showUserProfileDropdown="false">
    <span class="p-button p-button-rounded text-primary bg-white font-semibold border-0"> COMPANY DATA SAMPLE </span>
  </TheHeader>
  <TheContent>
    <MarginWrapper>
      <div class="grid">
        <div class="col-12 bg-green-500 p-0 mt-3">
          <p class="text-white font-semibold flex justify-content-center">
            <i class="material-icons pr-2 flex align-items-center" aria-hidden="true">check_circle</i>
            <span class="pr-2 flex align-items-center">Join Dataland with other people to access all the data.</span>
            <router-link
              to="/"
              class="p-button bg-white border-0 uppercase text-green-500 d-letters flex align-items-center no-underline"
              >Join for free</router-link
            >
          </p>
        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper class="text-left">
      <BackButton class="mt-3" />
    </MarginWrapper>
    <MarginWrapper>
      <div class="grid align-items-end">
        <div class="col-9">
          <CompanyInformation :companyID="companyID" />
        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper bgClass="surface-800">
      <EuTaxonomyData :companyID="companyID" />
    </MarginWrapper>
  </TheContent>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import { ApiClientProvider } from "@/services/ApiClients";
import EuTaxonomyData from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyData";
import CompanyInformation from "@/components/pages/CompanyInformation";
export default {
  name: "CompanyAssociatedEuTaxonomyDataSample",
  components: {
    CompanyInformation,
    EuTaxonomyData,
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
  },
  data: () => ({
    companyID: null,
  }),
  created() {
    this.queryCompany();
  },
  inject: ["getKeycloakPromise"],
  methods: {
    async queryCompany() {
      try {
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getCompanyDataControllerApi();
        const companyResponse = await companyDataControllerApi.getTeaserCompanies();
        console.log(companyResponse);
        this.companyID = companyResponse.data[0];
      } catch (error) {
        console.error(error);
      }
    },
  },
};
</script>
