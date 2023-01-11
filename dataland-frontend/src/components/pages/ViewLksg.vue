<template>
  <ViewFrameworkBase :companyID="companyID" dataType="lksg" @updateDataId="receiveDataId">
    <template v-if="frameworkDataId">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :dataID="frameworkDataId" />
        </div>
      </div>
    </template>
    <div v-if="frameworkDataId === null" class="col-12 text-left">
      <h2>No LKSG data</h2>
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
    console.log(`LKSG DataID`, this.frameworkDataId);
  },
  methods: {
    receiveDataId(id: undefined) {
      this.frameworkDataId = id;
    },
  },
});
</script>
