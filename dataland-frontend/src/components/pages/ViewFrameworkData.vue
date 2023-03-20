<template>
  <ViewSingleDatasetDisplayBase
    v-if="singleViewFrameworks.includes(dataType)"
    :dataType="dataType"
    :companyId="companyId"
    :dataId="dataId"
    :reportingPeriod="reportingPeriod"
  />
  <ViewMultipleDatasetsDisplayBase
    v-if="multiViewFrameworks.includes(dataType)"
    :dataType="dataType"
    :companyId="companyId"
    :dataId="dataId"
    :reportingPeriod="reportingPeriod"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DataTypeEnum } from "@clients/backend";
import ViewSingleDatasetDisplayBase from "@/components/generics/ViewSingleDatasetDisplayBase.vue";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";

export default defineComponent({
  name: "ViewFrameworkData",
  components: { ViewSingleDatasetDisplayBase, ViewMultipleDatasetsDisplayBase },
  props: {
    companyId: {
      type: String,
    },
    dataType: {
      type: String,
    },
    dataId: {
      type: String,
    },
    reportingPeriod: {
      type: String,
    },
  },

  data() {
    return {
      singleViewFrameworks: [DataTypeEnum.EutaxonomyNonFinancials, DataTypeEnum.EutaxonomyFinancials] as string[],
      multiViewFrameworks: [DataTypeEnum.Lksg, DataTypeEnum.Sfdr] as string[],
    };
  },
});
</script>
