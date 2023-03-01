<template>
  <ViewSingleDatasetDisplayBase
    :company-id="companyID"
    :data-id="dataId"
    :reporting-period="reportingPeriod"
    :data-type="DataTypeEnum.EutaxonomyFinancials"
    data-descriptor="EU-Taxonomy data for financial companies"
    title="EU Taxonomy Data"
    @updateDataIdOfDatasetToDisplay="handleUpdatedDataIdToDisplay"
  >
    <EuTaxonomyPanelFinancials :dataID="dataIdOfDisplayedDataset" />
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
