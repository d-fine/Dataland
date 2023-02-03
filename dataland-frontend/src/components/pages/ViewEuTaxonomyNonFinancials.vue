<template>
  <ViewFrameworkBase
    :companyID="companyID"
    :dataType="DataTypeEnum.EutaxonomyNonFinancials"
    @updateDataId="handleReceivedListOfDataIds"
  >
    <template v-if="dataIdToDisplay">
      <div class="grid">
        <div class="col-12 text-left">
          <h2 class="mb-0">EU Taxonomy Data</h2>
        </div>
        <div class="col-6 text-left">
          <p class="font-semibold m-0">2021</p>
          <p class="font-semibold text-gray-800 mt-0">Data from company report.</p>
        </div>
      </div>
      <div class="grid">
        <div v-if="dataIdToDisplay" class="col-7">
          <EuTaxonomyPanelNonFinancials :dataID="dataIdToDisplay" />
        </div>
      </div>
    </template>
    <div v-if="waitingForDataIdsAndChoosingDataIdToDisplay" class="col-12 text-left">
      <h2>Checking if EU-taxonomy data for non financial companies available...</h2>
    </div>
    <div
      v-if="!waitingForDataIdsAndChoosingDataIdToDisplay && listOfReceivedEuTaxoNonFinanicalsDataIds.length === 0"
      class="col-12 text-left"
    >
      <h2>No EU-Taxonomy data for non financial companies present</h2>
    </div>
    <div v-if="!isQueryParamDataIdValid">
      <h2>
        There is no EU-Taxonomy data for non financial companies available for the data ID you provided in the URL.
      </h2>
    </div>
  </ViewFrameworkBase>
  <DatalandFooter />
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import EuTaxonomyPanelNonFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelNonFinancials.vue";
import { defineComponent } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { useRoute } from "vue-router";
import { DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "ViewEuTaxonomyNonFinancials",
  components: { ViewFrameworkBase, EuTaxonomyPanelNonFinancials, DatalandFooter },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      waitingForDataIdsAndChoosingDataIdToDisplay: true,
      listOfReceivedEuTaxoNonFinanicalsDataIds: [] as string[],
      dataIdToDisplay: "",
      route: useRoute(),
      isQueryParamDataIdValid: true,
      DataTypeEnum,
    };
  },
  methods: {
    /**
     * Handles changes in the provided data IDs by storing and picking a dataset to display
     *
     * @param receivedEuTaxoNonFinanicalsDataIds Received EU Taxonomy for non financial companies data IDs
     */
    handleReceivedListOfDataIds(receivedEuTaxoNonFinanicalsDataIds: []) {
      this.listOfReceivedEuTaxoNonFinanicalsDataIds = receivedEuTaxoNonFinanicalsDataIds;
      this.chooseDataIdToDisplayBasedOnQueryParam();
      this.waitingForDataIdsAndChoosingDataIdToDisplay = false;
    },
    /**
     * Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    chooseDataIdToDisplayBasedOnQueryParam() {
      const singleQueryDataId = this.route.query.dataId as string;
      if (singleQueryDataId) {
        if (this.listOfReceivedEuTaxoNonFinanicalsDataIds.includes(singleQueryDataId)) {
          this.dataIdToDisplay = singleQueryDataId;
        } else {
          this.isQueryParamDataIdValid = false;
        }
      } else {
        this.dataIdToDisplay = this.listOfReceivedEuTaxoNonFinanicalsDataIds[0];
      }
    },
  },
});
</script>
