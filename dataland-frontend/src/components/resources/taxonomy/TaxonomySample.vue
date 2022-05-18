<template>
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
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import CompanyInformation from "@/components/resources/company/CompanyInformation";
import Button from "primevue/button";
import TaxonomyData from "@/components/resources/taxonomy/TaxonomyData";

export default {
  name: "TaxonomySample",
  components: {
    MarginWrapper, CompanyInformation, Button, TaxonomyData
  },
  props: {
    companyID: {
      type: String
    }
  }
}
</script>