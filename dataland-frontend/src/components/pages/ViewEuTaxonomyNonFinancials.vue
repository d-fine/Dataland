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
    <div v-if="!waitingForDataIdsAndChoosingDataIdToDisplay && !dataIdToDisplay" class="col-12 text-left">
      <h2>No EU-Taxonomy data for non financial companies present</h2>
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
      DataTypeEnum,
    };
  },
  methods: {
    /**
     * TODO adjust
     * Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
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
        this.dataIdToDisplay = singleQueryDataId;
      } else {
        this.dataIdToDisplay = this.listOfReceivedEuTaxoNonFinanicalsDataIds[0];
      }
    },
  },
});
</script>
