<template>
  <ViewMultipleDatasetsDisplayBase
    v-if="multiViewFrameworks.includes(dataType)"
    :dataType="dataType"
    :companyId="companyId"
    :dataId="dataId"
    :reportingPeriod="reportingPeriod"
    :viewInPreviewMode="viewInPreviewMode"
  />
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import type { DataTypeEnum } from '@clients/backend';
import ViewMultipleDatasetsDisplayBase from '@/components/generics/ViewMultipleDatasetsDisplayBase.vue';
import { getAllFrameworkIdentifiers } from '@/frameworks/FrontendFrameworkRegistry';
import router from '@/router';

export default defineComponent({
  name: 'ViewFrameworkData',
  components: { ViewMultipleDatasetsDisplayBase },
  props: {
    companyId: {
      type: String,
      required: true,
    },
    dataType: {
      type: String as PropType<DataTypeEnum>,
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
      void router.push('/nocontent');
    },
  },
  mounted() {
    if (!this.dataType || !this.multiViewFrameworks.includes(this.dataType)) {
      return this.gotoNotFound();
    }
  },
  computed: {
    multiViewFrameworks(): string[] {
      return getAllFrameworkIdentifiers();
    },
  },
});
</script>
