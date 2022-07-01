<template>
  <div class="d-header grid fixed top-0 col-12 surface-900 align-items-center h-4rem w-screen">
    <div class="col-4 text-left">
      <router-link to="/">
        <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" class="pl-3 pt-2" />
      </router-link>
    </div>
    <div class="col-4">
      <span v-if="sampleData" class="p-button p-button-rounded text-primary bg-white font-semibold border-0">
        COMPANY DATA SAMPLE
      </span>
    </div>
  </div>
</template>
<script>
export default {
  name: "TheHeader",
  props: {
    sampleData: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
