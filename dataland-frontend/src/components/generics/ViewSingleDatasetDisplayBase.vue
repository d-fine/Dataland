<template>
  <ViewFrameworkBase :companyID="companyId" :dataType="dataType" @updateDataId="handleReceivedListOfDataIds">
    <div v-if="dataIdToDisplay">
      <div class="grid">
        <div class="col-12 text-left">
          <h2 class="mb-0">{{ title }}</h2>
        </div>
        <div class="col-6 text-left">
          <p class="font-semibold m-0">2021</p>
          <p class="font-semibold text-gray-800 mt-0">Data from company report.</p>
        </div>
      </div>
      <div class="grid">
        <div class="col-7">
          <slot></slot>
        </div>
      </div>
    </div>
    <div v-if="waitingForDataIdsAndChoosingDataIdToDisplay" class="col-12 text-left">
      <h2>Checking if {{ dataDescriptor }} available...</h2>
    </div>
    <div
      v-if="!waitingForDataIdsAndChoosingDataIdToDisplay && listOfReceivedDataIds.length === 0"
      class="col-12 text-left"
    >
      <h2>No {{ dataDescriptor }} present</h2>
    </div>
    <div v-if="!isQueryParamDataIdValid">
      <h2>There is no {{ dataDescriptor }} available for the data ID you provided in the URL.</h2>
    </div>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { defineComponent } from "vue";
import { useRoute } from "vue-router";

export default defineComponent({
  name: "ViewSingleDatasetDisplayBase",
  components: { ViewFrameworkBase },
  props: {
    companyId: {
      type: String,
    },
    dataType: {
      type: String,
    },
    dataDescriptor: {
      type: String,
    },
    title: {
      type: String,
    },
    dataIdToDisplay: {
      type: String,
      default: "",
    },
  },
  emits: ["update:dataIdToDisplay"],
  data() {
    return {
      waitingForDataIdsAndChoosingDataIdToDisplay: true,
      listOfReceivedDataIds: [] as string[],
      route: useRoute(),
      isQueryParamDataIdValid: true,
    };
  },
  methods: {
    /**
     * Handles changes in the provided data IDs by storing and picking a dataset to display
     *
     * @param receivedDataIds Received data IDs
     */
    handleReceivedListOfDataIds(receivedDataIds: []) {
      this.listOfReceivedDataIds = receivedDataIds;
      this.chooseDataIdToDisplayBasedOnQueryParam();
      this.waitingForDataIdsAndChoosingDataIdToDisplay = false;
    },
    /**
     * Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    chooseDataIdToDisplayBasedOnQueryParam() {
      const singleQueryDataId = this.route.query.dataId as string;
      if (singleQueryDataId) {
        if (this.listOfReceivedDataIds.includes(singleQueryDataId)) {
          this.$emit("update:dataIdToDisplay", singleQueryDataId);
        } else {
          this.isQueryParamDataIdValid = false;
        }
      } else {
        this.$emit("update:dataIdToDisplay", this.listOfReceivedDataIds[0]);
      }
    },
  },
});
</script>
