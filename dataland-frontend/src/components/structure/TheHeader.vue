<template>
  <div class="d-header m-0 fixed top-0 surface-900 h-4rem w-full grid align-items-center">
    <div class="col-4 text-left col-4">
      <router-link to="/">
        <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" class="pl-3" />
      </router-link>
    </div>

    <div class="col-4">
      <span v-if="sampleData" class="p-button p-button-rounded text-primary bg-white font-semibold border-0">
        COMPANY DATA SAMPLE
      </span>
    </div>

    <div class="col-4 flex justify-content-end" v-if="!sampleData">
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
