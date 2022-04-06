<template>
  <EuTaxoSearchBar/>
  <div class="grid align-items-end">
    <div class="col-9 ml-4">
      <CompanyInformation :companyID="companyID"/>
    </div>
    <div class="col-2 mr-4 pb-4">
      <Button label="Get Report" class="uppercase p-button">Get Report
        <i class="material-icons pl-3" aria-hidden="true">arrow_drop_down</i>
      </Button>
    </div>
  </div>
  <div class="grid m-3 surface-800">
    <div class="col-12">
      <TaxonomyData :companyID="companyID"/>
    </div>
  </div>
</template>

<script>
import CompanyInformation from "@/components/pages/company/CompanyInformation";
import Button from "primevue/button";
import TaxonomyData from "@/components/pages/taxonomy/TaxonomyData";
import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";

export default {
  name: "CompanyTaxonomy",
  components: {EuTaxoSearchBar, TaxonomyData, CompanyInformation, Button},
  props: {
    companyID: {
      type: Number
    }
  }

}
</script>