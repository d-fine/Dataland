<template>
  <AuthenticationWrapper :disable-authentication-wrapper="viewInPreviewMode">
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
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";

export default defineComponent({
  name: "ViewFrameworkData",
  components: { AuthenticationWrapper, ViewMultipleDatasetsDisplayBase },
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
    if (!this.dataType) {
      return this.gotoNotFound();
    }
    if (!this.multiViewFrameworks.includes(this.dataType)) {
      return this.gotoNotFound();
    }
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
      multiViewFrameworks: [
        DataTypeEnum.EutaxonomyFinancials,
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
