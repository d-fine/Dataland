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
            <div class="col-3">
              <Calendar inputId="basic" v-model="date1" autocomplete="off"  style="border: 3px solid red"/>
            </div>
            <div class="col-7">
              <LksgPanel :dataID="frameworkDataId" />
            </div>
          </div>

    <div class="col-12 text-left">
      <h2>Loading...</h2>
    </div>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import Calendar from "primevue/calendar";
import { defineComponent } from "vue";

export default defineComponent({
  name: "ViewEuTaxonomyFinancials",
  components: { ViewFrameworkBase, LksgPanel, Calendar },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      date1: null,
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
