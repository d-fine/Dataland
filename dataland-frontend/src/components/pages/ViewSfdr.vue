<template>
  <ViewFrameworkBase :companyID="companyID" dataType="sfdr" @updateDataId="handleReceivedListOfDataIds">
    <template v-if="receivedSfdrDataIds.length > 0">
      <div class="grid">
        <div class="col-12">
          <SfdrPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if SFDR data is available...</h2>
    </div>
    <div v-if="!loading && receivedSfdrDataIds.length === 0" class="col-12 text-left">
      <h2>No SFDR data</h2>
    </div>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "ViewSfdr",
  components: { ViewFrameworkBase, SfdrPanel },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      receivedSfdrDataIds: [] as string[],
    };
  },
  methods: {
    /**
     * TODO Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedSfdrDataIds Received Sfdr data IDs
     */
    handleReceivedListOfDataIds(receivedSfdrDataIds: []) {
      this.receivedSfdrDataIds = receivedSfdrDataIds;
      this.loading = false;
    },
  },
});
</script>
