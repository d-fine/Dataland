<template>
  <ViewSingleDatasetDisplayBase
    :company-id="companyID"
    :data-type="DataTypeEnum.EutaxonomyNonFinancials"
    v-model:data-id-to-display="dataIdToDisplay"
    data-descriptor="EU-Taxonomy data for non financial companies"
    title="EU Taxonomy Data"
  >
    <EuTaxonomyPanelNonFinancials :dataID="dataIdToDisplay" />
  </ViewSingleDatasetDisplayBase>
</template>

<script lang="ts">
import EuTaxonomyPanelNonFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelNonFinancials.vue";
import { defineComponent } from "vue";
import { DataTypeEnum } from "@clients/backend";
import ViewSingleDatasetDisplayBase from "@/components/generics/ViewSingleDatasetDisplayBase.vue";

export default defineComponent({
  name: "ViewEuTaxonomyNonFinancials",
  components: { ViewSingleDatasetDisplayBase, EuTaxonomyPanelNonFinancials },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      dataIdToDisplay: "",
      DataTypeEnum,
    };
  },
});
</script>
