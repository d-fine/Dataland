<template>
  <PrimeButton
    class="uppercase p-button p-button-sm d-letters mr-3"
    label="Request Data"
    icon="pi pi-question"
    @click="redirectToRequestDataPage"
  />
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
export default defineComponent({
  name: "RequestDataButton",
  components: { PrimeButton },
  methods: {
    /**
     * Executes router push to the request data page
     */
    async redirectToRequestDataPage() {
      await this.$router.push("/requests");
    },
  },
});
</script>

<style scoped></style>
