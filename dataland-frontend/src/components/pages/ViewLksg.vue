<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="receiveDataId">
<!--    <template v-if="frameworkDataId">-->
<!--      <div class="grid">-->
<!--        <div class="col-12 text-left">-->
<!--          <h2 class="mb-0">EU Taxonomy Data</h2>-->
<!--        </div>-->
<!--        <div class="col-6 text-left">-->
<!--          <p class="font-semibold m-0">2021</p>-->
<!--          <p class="font-semibold text-gray-800 mt-0">Data from company report.</p>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div class="grid">-->
<!--        <div class="col-7">-->
<!--          <EuTaxonomyPanelFinancials :dataID="frameworkDataId" />-->
<!--        </div>-->
<!--      </div>-->
<!--    </template>-->
<!--    <div v-if="frameworkDataId === null" class="col-12 text-left">-->
<!--      <h2>No EU-Taxonomy data for financial companies present</h2>-->
<!--    </div>-->
    <div class="grid">
            <div class="col-12">
              <h3>{{ JSON.stringify(frameworkDataId) }}</h3>



              <LksgPanel :dataID="frameworkDataId" />
            </div>
          </div>

  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "ViewLksg",
  components: { ViewFrameworkBase, LksgPanel },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      frameworkDataId: undefined,
    };
  },
  mounted() {
    console.log(`LKSG DataID`, this.frameworkDataId)
  },
  methods: {
    receiveDataId(id: undefined) {
      this.frameworkDataId = id;
    },
  },
});
</script>
