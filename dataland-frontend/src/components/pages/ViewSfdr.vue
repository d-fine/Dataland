<template>
  <ViewMultipleDatasetsDisplayBase :companyID="companyID" :dataType="DataTypeEnum.Lksg">
    <SfdrPanel :companyId="companyID" />
  </ViewMultipleDatasetsDisplayBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import SfdrPanel from "@/components/resources/frameworkDataSearch/lksg/SfdrPanel.vue";
import { defineComponent } from "vue";
import { DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "ViewLksg",
  components: { ViewFrameworkBase, ViewMultipleDatasetsDisplayBase, SfdrPanel },
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
