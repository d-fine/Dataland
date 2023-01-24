<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="handleReceivedListOfDataIds">
    <template v-if="receivedLksgDataIds">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="receivedLksgDataIds === null" class="col-12 text-left">
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
      receivedLksgDataIds: [] as string[],
    };
  },
  methods: {
    handleReceivedListOfDataIds(receivedLksgDataIds: []) {
      this.receivedLksgDataIds = receivedLksgDataIds;
    },
  },
});
</script>
