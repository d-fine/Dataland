<template>
  <TheHeader :sampleData="true"/>
  <TheContent>
    <MarginWrapper>
      <div class="grid">
        <div class="col-12 bg-green-500 p-0">
          <p class="text-white font-semibold flex justify-content-center">
            <i class="material-icons pr-2 flex align-items-center" aria-hidden="true">check_circle</i>
            <span class="pr-2 flex align-items-center">Join Dataland with others 1384 people to access all the data.</span>
            <router-link to="/" class="p-button bg-white border-0 uppercase text-green-500 d-letters flex align-items-center no-underline">Join for free</router-link>
          </p>

        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper class="text-left">
      <BackButton/>
    </MarginWrapper>
    <EuTaxoSearchBar/>
    <MarginWrapper>
      <div class="grid align-items-end">
        <div class="col-9">
          <CompanyInformation :companyID="companyIdList[companyIndex]"/>
        </div>
        <div class="col-3 pb-4 text-right">
          <Button label="Export Data" class="uppercase p-button">Export Data
            <i class="material-icons pl-3" aria-hidden="true">arrow_drop_down</i>
          </Button>
        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper bgClass="surface-800">
      <TaxonomyData :companyID="companyIdList[companyIndex]"/>
    </MarginWrapper>
  </TheContent>
</template>

<script>
import Button from "primevue/button";
import CompanyInformation from "@/components/resources/company/CompanyInformation";
import TaxonomyData from "@/components/resources/taxonomy/TaxonomyData";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import {CompanyDataControllerApi} from "@/../build/clients/backend/api"
import {ApiWrapper} from "@/services/ApiWrapper"
const companyDataControllerApi = new CompanyDataControllerApi()
const getCompaniesWrapper = new ApiWrapper(companyDataControllerApi.getCompanies)
export default {
  name: "CompanyTaxonomy",
  components: {
    TheContent,
    TheHeader, BackButton, MarginWrapper, EuTaxoSearchBar, TaxonomyData, CompanyInformation, Button
  },
  data: () => ({
      companyIdList: []
  }),
  props: {
    companyIndex: {
      type: Number,
      default: 1
    }
  },
  created() {
    this.getCompanyIDs()
  },
  methods: {
    async getCompanyIDs() {
      try {
        const companyList = await getCompaniesWrapper.perform("", "", true)
        this.companyIdList = companyList.data.map(element => element.companyId)
      } catch (error) {
        this.companyIdList = []
      }
    }
  }
}
</script>