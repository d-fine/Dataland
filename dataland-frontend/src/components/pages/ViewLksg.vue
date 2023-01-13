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
      frameworkDataId: [] as string[],
    };
  },
  methods: {
    receiveDataId(id: string | []) {
      let dataIdsArray = [] as string[];
      if (Object.prototype.toString.call(id) === "[object String]") {
        dataIdsArray.push(id as string);
      } else if (Array.isArray(id)) {
        dataIdsArray = id.map((el) => (el as { dataId: string; dataType: string; companyId: string }).dataId);
      }
      this.frameworkDataId = dataIdsArray;
    },
  },
});
</script>
