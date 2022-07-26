<template>
  <MarginWrapper>
    <div class="grid align-items-end">
      <div class="col-9">
        <CompanyInformation :companyID="companyID" />
      </div>
    </div>
  </MarginWrapper>
  <MarginWrapper bgClass="surface-800">
    <TaxonomyData :companyID="companyID" />
  </MarginWrapper>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import CompanyInformation from "@/components/pages/CompanyInformation";
import TaxonomyData from "@/components/resources/taxonomy/TaxonomyData";

export default {
  name: "TaxonomySample",
  components: {
    MarginWrapper,
    CompanyInformation,
    TaxonomyData,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
