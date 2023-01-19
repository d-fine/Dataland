<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="handleReceivedListOfDataIds">
    <template v-if="listOfReceivedLksgDataIds">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :dataID="listOfReceivedLksgDataIds" />
        </div>
      </div>
    </template>
    <div v-if="listOfReceivedLksgDataIds === null" class="col-12 text-left">
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
      listOfReceivedLksgDataIds: [] as string[],
    };
  },
  methods: {
    handleReceivedListOfDataIds(receivedLksgDataIds: []) {
      this.listOfReceivedLksgDataIds = receivedLksgDataIds
    },
  },
});
</script>
