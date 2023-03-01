<template>
  <ViewSingleDatasetDisplayBase
    :company-id="companyID"
    :data-id="dataId"
    :reporting-period="reportingPeriod"
    :data-type="DataTypeEnum.EutaxonomyNonFinancials"
    data-descriptor="EU-Taxonomy data for non financial companies"
    title="EU Taxonomy Data"
    @updateDataIdOfDatasetToDisplay="handleUpdatedDataIdToDisplay"
  >
    <EuTaxonomyPanelNonFinancials :dataID="dataIdOfDisplayedDataset" />
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
    dataId: {
      type: String,
    },
    reportingPeriod: {
      type: String,
    },
  },
  data() {
    return {
      dataIdOfDisplayedDataset: null as string | null,
      DataTypeEnum,
    };
  },
  methods: {
    /**
     * Method to handle an updated data ID of the displayed data set
     *
     * @param updatedDataId The new/updated data ID
     */
    handleUpdatedDataIdToDisplay(updatedDataId: string) {
      console.log("updated data Id"); // TODO debugging
      console.log(updatedDataId); // TODO debugging
      this.dataIdOfDisplayedDataset = updatedDataId;
    },
  },
});
</script>
