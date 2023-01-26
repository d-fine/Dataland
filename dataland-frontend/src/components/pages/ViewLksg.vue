<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="handleReceivedListOfDataIds">
    <template v-if="receivedLksgDataIds.length > 0">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if LkSG data available...</h2>
    </div>
    <div v-if="!loading && receivedLksgDataIds.length === 0" class="col-12 text-left">
      <h2>No LkSG data</h2>
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
      loading: true,
      receivedLksgDataIds: [] as string[],
    };
  },
  methods: {
    /**
     * Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedLksgDataIds
     */
    handleReceivedListOfDataIds(receivedLksgDataIds: []) {
      this.receivedLksgDataIds = receivedLksgDataIds;
      this.loading = false;
    },
  },
});
</script>
