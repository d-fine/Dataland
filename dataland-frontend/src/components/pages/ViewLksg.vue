<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="handleReceivedListOfDataIds">
    <template v-if="lksgDataExists">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="!lksgDataExists" class="col-12 text-left">
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
      lksgDataExists: false,
    };
  },
  methods: {
    handleReceivedListOfDataIds(receivedLksgDataIds: []) {
      this.lksgDataExists = receivedLksgDataIds.length > 0;
    },
  },
});
</script>
