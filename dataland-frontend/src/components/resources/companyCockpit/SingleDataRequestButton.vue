<template>
  <PrimeButton @click="onClick" class="uppercase p-button p-button-sm" data-test="singleDataRequestButton">
    <span class="d-letters pl-2"> Request Data </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import { type DataTypeEnum } from '@clients/backend';
import { type RouteLocationNormalizedLoaded } from 'vue-router';
import router from '@/router';

export default defineComponent({
  name: 'SingleDataRequestButton',
  components: { PrimeButton },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  methods: {
    /**
     * navigates to the single data request page
     * @returns a router push
     */
    onClick() {
      const thisCompanyId = this.companyId;
      const currentRoute: RouteLocationNormalizedLoaded = router.currentRoute.value;
      const dataType = currentRoute.params.dataType;
      const preSelectedFramework = dataType ? (dataType as DataTypeEnum) : '';
      return router.push({
        path: `/singledatarequest/${thisCompanyId}`,
        query: {
          preSelectedFramework: preSelectedFramework,
        },
      });
    },
  },
});
</script>
