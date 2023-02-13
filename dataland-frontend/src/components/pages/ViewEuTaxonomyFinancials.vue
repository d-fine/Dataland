<template>
  <ViewSingleDatasetDisplayBase
    :company-id="companyID"
    :data-type="DataTypeEnum.EutaxonomyFinancials"
    v-model:data-id-to-display="dataIdToDisplay"
    data-descriptor="EU-Taxonomy data for financial companies"
    title="EU Taxonomy Data"
  >
    <EuTaxonomyPanelFinancials :dataID="dataIdToDisplay" />
  </ViewSingleDatasetDisplayBase>
</template>

<script lang="ts">
import EuTaxonomyPanelFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelFinancials.vue";
import { defineComponent } from "vue";
import ViewSingleDatasetDisplayBase from "@/components/generics/ViewSingleDatasetDisplayBase.vue";
import { DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "ViewEuTaxonomyFinancials",
  components: { ViewSingleDatasetDisplayBase, EuTaxonomyPanelFinancials },
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
