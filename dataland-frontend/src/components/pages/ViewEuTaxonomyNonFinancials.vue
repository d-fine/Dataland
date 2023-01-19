<template>
  <ViewFrameworkBase
    :companyID="companyID"
    dataType="eutaxonomy-non-financials"
    @updateDataId="handleReceivedListOfDataIds"
  >
    <template v-if="listOfReceivedEuTaxoNonFinanicalsDataIds">
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
    <div v-if="listOfReceivedEuTaxoNonFinanicalsDataIds === null" class="col-12 text-left">
      <h2>No EU-Taxonomy data for non financial companies present</h2>
    </div>
    <div v-if="listOfReceivedEuTaxoNonFinanicalsDataIds === undefined" class="col-12 text-left">
      <h2>Loading...</h2>
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
      listOfReceivedEuTaxoNonFinanicalsDataIds: [] as string[],
    };
  },
  methods: {
    handleReceivedListOfDataIds(receivedEuTaxoNonFinanicalsDataIds: []) {
      this.listOfReceivedEuTaxoNonFinanicalsDataIds = receivedEuTaxoNonFinanicalsDataIds;
    },
  },
});
</script>
