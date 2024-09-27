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
import { defineComponent } from 'vue';
import { DataTypeEnum } from '@clients/backend';
import ViewMultipleDatasetsDisplayBase from '@/components/generics/ViewMultipleDatasetsDisplayBase.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { getAllFrameworkIdentifiers } from '@/frameworks/FrontendFrameworkRegistry';

export default defineComponent({
  name: 'ViewFrameworkData',
  components: { AuthenticationWrapper, ViewMultipleDatasetsDisplayBase },
  props: {
    companyId: {
      type: String,
      required: true,
    },
    dataType: {
      type: String,
      required: true,
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
  methods: {
    /**
     * Navigate to the not found page.
     */
    gotoNotFound() {
      void this.$router.push('/nocontent');
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
  computed: {
    multiViewFrameworks(): string[] {
      const standardMultiViewFrameworks = [DataTypeEnum.EuTaxonomyFinancials, DataTypeEnum.P2p] as string[];

      for (const frameworkId of getAllFrameworkIdentifiers()) {
        standardMultiViewFrameworks.push(frameworkId);
      }
      return standardMultiViewFrameworks;
    },
  },
});
</script>
