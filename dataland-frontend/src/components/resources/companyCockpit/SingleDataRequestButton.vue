<template>
  <PrimeButton @click="onClick" class="uppercase p-button p-button-sm mr-3" data-test="singleDataRequestButton">
    <span class="d-letters pl-2"> Request Data </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "SingleDataRequestButton",
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
      return this.$router.push({
        path: `/singledatarequest/${thisCompanyId}`,
      });
    },
  },
});
</script>
