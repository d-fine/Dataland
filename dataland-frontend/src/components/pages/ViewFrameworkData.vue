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
  mounted() {
    if (!this.dataType) return this.gotoNotFound();
    if (!this.singleViewFrameworks.includes(this.dataType) && !this.multiViewFrameworks.includes(this.dataType))
      return this.gotoNotFound();
  },
  methods: {
    /**
     * Navigate to the not found page.
     */
    gotoNotFound() {
      void this.$router.push("/nocontent");
    },
  },
  data() {
    return {
      singleViewFrameworks: [DataTypeEnum.EutaxonomyFinancials] as string[],
      multiViewFrameworks: [
        /* DataTypeEnum.EutaxonomyNonFinancials, */ DataTypeEnum.Lksg,
        DataTypeEnum.Sfdr,
        DataTypeEnum.P2p,
        DataTypeEnum.Sme,
      ] as string[],
    };
  },
});
</script>
