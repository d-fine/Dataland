<template>
  <ViewMultipleDatasetsDisplayBase :companyID="companyID" :dataType="DataTypeEnum.Lksg">
    <LksgPanel :companyId="companyID" />
  </ViewMultipleDatasetsDisplayBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { defineComponent } from "vue";
import { DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "ViewLksg",
  components: { ViewFrameworkBase, ViewMultipleDatasetsDisplayBase, LksgPanel },
  props: {
    companyID: {
      type: String,
    },
  },

  data() {
    return {
      DataTypeEnum,
    };
  },
});
</script>
