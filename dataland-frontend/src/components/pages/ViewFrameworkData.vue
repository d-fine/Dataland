<template>
  <AuthenticationWrapper :disable-authentication-wrapper="viewInPreviewMode">
    <ViewSingleDatasetDisplayBase
      v-if="singleViewFrameworks.includes(dataType)"
      :dataType="dataType"
      :companyId="companyId"
      :dataId="dataId"
      :reportingPeriod="reportingPeriod"
      :viewInPreviewMode="viewInPreviewMode"
    />
    <ViewMultipleDatasetsDisplayBase
      v-if="multiViewFrameworks.includes(dataType)"
      :dataType="dataType"
      :companyId="companyId"
      :dataId="dataId"
      :reportingPeriod="reportingPeriod"
      :viewInPreviewMode="viewInPreviewMode"
    />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DataTypeEnum } from "@clients/backend";
import ViewSingleDatasetDisplayBase from "@/components/generics/ViewSingleDatasetDisplayBase.vue";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";

export default defineComponent({
  name: "ViewFrameworkData",
  components: { AuthenticationWrapper, ViewSingleDatasetDisplayBase, ViewMultipleDatasetsDisplayBase },
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
    viewInPreviewMode: {
      type: Boolean,
      default: false,
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
        DataTypeEnum.EutaxonomyNonFinancials,
        DataTypeEnum.Lksg,
        DataTypeEnum.Sfdr,
        DataTypeEnum.P2p,
        DataTypeEnum.Sme,
      ] as string[],
    };
  },
});
</script>
