<template>
  <EuTaxoSearchBar/>
  <MarginWrapper>
    <div class="grid align-items-end">
      <div class="col-9">
        <CompanyInformation :companyID="companyID"/>
      </div>
      <div class="col-3 pb-4 text-right">
        <Button label="Get Report" class="uppercase p-button">Get Report
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
import CompanyInformation from "@/components/pages/company/CompanyInformation";
import Button from "primevue/button";
import TaxonomyData from "@/components/pages/taxonomy/TaxonomyData";
import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "CompanyTaxonomy",
  components: {MarginWrapper, EuTaxoSearchBar, TaxonomyData, CompanyInformation, Button},
  props: {
    companyID: {
      type: Number
    }
  }

}
</script>