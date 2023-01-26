<template>
  <ViewFrameworkBase
    :companyID="companyID"
    dataType="eutaxonomy-financials"
    @updateDataId="handleReceivedListOfDataIds"
  >
    <template v-if="listOfReceivedEuTaxoFinancialsDataIds.length > 0">
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
          <EuTaxonomyPanelFinancials :dataID="listOfReceivedEuTaxoFinancialsDataIds[0]" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if EU-taxonomy data for financial companies available...</h2>
    </div>
    <div v-if="!loading && listOfReceivedEuTaxoFinancialsDataIds.length === 0" class="col-12 text-left">
      <h2>No EU-Taxonomy data for financial companies present</h2>
    </div>
  </ViewFrameworkBase>
  <DatalandFooter />
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import EuTaxonomyPanelFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelFinancials.vue";
import { defineComponent } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default defineComponent({
  name: "ViewEuTaxonomyFinancials",
  components: { ViewFrameworkBase, EuTaxonomyPanelFinancials, DatalandFooter },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      listOfReceivedEuTaxoFinancialsDataIds: [] as string[],
    };
  },
  methods: {
    handleReceivedListOfDataIds(receivedEuTaxoFinancialsDataIds: []) {
      this.listOfReceivedEuTaxoFinancialsDataIds = receivedEuTaxoFinancialsDataIds;
      this.loading = false;
    },
  },
});
</script>
