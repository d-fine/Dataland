<template>
  <ViewFrameworkBase :companyID="companyID" dataType="eutaxonomy-financials" @updateDataId="receiveDataId">
    <template v-if="frameworkDataId">
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
          <EuTaxonomyPanelFinancials :dataID="frameworkDataId[0]" />
        </div>
      </div>
    </template>
    <div v-if="frameworkDataId === null" class="col-12 text-left">
      <h2>No EU-Taxonomy data for financial companies present</h2>
    </div>
    <div v-if="frameworkDataId === undefined" class="col-12 text-left">
      <h2>Loading...</h2>
    </div>
  </ViewFrameworkBase>
  <DatalandFooter />
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import EuTaxonomyPanelFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelFinancials.vue";
import { defineComponent } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { DataMetaInformation } from "@clients/backend";

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
      frameworkDataId: [] as string[],
    };
  },
  methods: {
    receiveDataId(id: []) {
      this.frameworkDataId = id.map((el) => (el as DataMetaInformation).dataId);
    },
  },
});
</script>
