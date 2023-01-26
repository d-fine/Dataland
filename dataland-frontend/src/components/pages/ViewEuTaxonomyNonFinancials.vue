<template>
  <ViewFrameworkBase
    :companyID="companyID"
    dataType="eutaxonomy-non-financials"
    @updateDataId="handleReceivedListOfDataIds"
  >
    <template v-if="listOfReceivedEuTaxoNonFinanicalsDataIds.length > 0">
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
        <div class="col-7">
          <EuTaxonomyPanelNonFinancials :dataID="listOfReceivedEuTaxoNonFinanicalsDataIds[0]" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if EU-taxonomy data for non financial companies available...</h2>
    </div>
    <div v-if="!loading && listOfReceivedEuTaxoNonFinanicalsDataIds.length === 0" class="col-12 text-left">
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
      loading: true,
      listOfReceivedEuTaxoNonFinanicalsDataIds: [] as string[],
    };
  },
  methods: {
    /**
     * Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedEuTaxoNonFinanicalsDataIds
     */
    handleReceivedListOfDataIds(receivedEuTaxoNonFinanicalsDataIds: []) {
      this.listOfReceivedEuTaxoNonFinanicalsDataIds = receivedEuTaxoNonFinanicalsDataIds;
      this.loading = false;
    },
  },
});
</script>
