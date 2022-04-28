<template>
  <MarginWrapper class="text-left">
    <BackButton/>
  </MarginWrapper>
  <EuTaxoSearchBar/>
  <MarginWrapper>
    <div class="grid align-items-end">
      <div class="col-9">
        <CompanyInformation :companyID="companyID"/>
      </div>
      <div class="col-3 pb-4 text-right">
        <Button label="Export Data" class="uppercase p-button">Export Data
          <i class="material-icons pl-3" aria-hidden="true">arrow_drop_down</i>
        </Button>
      </div>
    </div>
  </MarginWrapper>
  <MarginWrapper bgClass="surface-800">
    <TaxonomyData :companyID="companyID"/>
  </MarginWrapper>
</template>

<script>
import Button from "primevue/button";
import CompanyInformation from "@/components/resources/company/CompanyInformation";
import TaxonomyData from "@/components/resources/taxonomy/TaxonomyData";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";

export default {
  name: "CompanyTaxonomy",
  components: {BackButton, MarginWrapper, EuTaxoSearchBar, TaxonomyData, CompanyInformation, Button},
  props: {
    companyID: {
      type: Number
    }
  }
}
</script>