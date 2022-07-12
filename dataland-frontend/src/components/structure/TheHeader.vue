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

    <div class="col-1 col-offset-3 max-w-full max-h-full">
      <UserProfileDropDown />
    </div>
  </div>
</template>
<script>
import UserProfileDropDown from "@/components/general/UserProfileDropDown";
export default {
  name: "TheHeader",
  components: { UserProfileDropDown },
  props: {
    sampleData: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
