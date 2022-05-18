<template>
  <TheHeader :sampleData="true"/>
  <TheContent>
    <MarginWrapper>
      <div class="grid">
        <div class="col-12 bg-green-500 p-0">
          <p class="text-white font-semibold flex justify-content-center">
            <i class="material-icons pr-2 flex align-items-center" aria-hidden="true">check_circle</i>
            <span class="pr-2 flex align-items-center">Join Dataland with other people to access all the data.</span>
            <router-link to="/" class="p-button bg-white border-0 uppercase text-green-500 d-letters flex align-items-center no-underline">Join for free</router-link>
          </p>

        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper class="text-left">
      <BackButton/>
    </MarginWrapper>
    <EuTaxoSearchBar/>
    <TaxonomySample :companyID="companyID" v-if="companyID"/>
  </TheContent>
</template>

<script>
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import {CompanyDataControllerApi} from "@/../build/clients/backend/api"
import {ApiWrapper} from "@/services/ApiWrapper"
import TaxonomySample from "@/components/resources/taxonomy/TaxonomySample";
const companyDataControllerApi = new CompanyDataControllerApi()
const getCompaniesWrapper = new ApiWrapper(companyDataControllerApi.getCompanies)
export default {
  name: "CompanyTaxonomy",
  components: {
    TaxonomySample,
    TheContent,
    TheHeader, BackButton, MarginWrapper, EuTaxoSearchBar
  },
  data: () => ({
    companyQuery: "Adidas",
    companyID: null
  }),
  created() {
    this.queryCompany()
  },
  methods: {
    async queryCompany() {
      try {
        const responseArray = await getCompaniesWrapper.perform(this.companyQuery, "", true)
        this.companyID = responseArray.data[0].companyId
      } catch (error) {
        await this.companyIdFallback()
        console.error(error)
      }
    },
    async companyIdFallback() {
      try {
        const companyResponse = await getCompaniesWrapper.perform("", "", true)
        this.companyID = companyResponse.data[0].companyId
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>