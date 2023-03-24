<template>
  <UserAuthenticationButtons v-if="showLogInButton" />
  <PrimeButton
    v-if="showRefreshButton"
    label="Refresh Session"
    class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
    name="refresh_session_button"
    @click="handleClickOnRefreshSession"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { UserAuthenticationButtons, PrimeButton },
  data() {
    return {
      showLogInButton: false,
      showRefreshButton: false,
    };
  },

  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      showLogInButton: boolean;
      showRefreshButton: boolean;
    };
    this.showLogInButton = dialogRefData.showLogInButton;
    this.showRefreshButton = dialogRefData.showRefreshButton;
  },

  methods: {
    /**
     * Handles the click-event on the "refresh session" button.
     */
    handleClickOnRefreshSession() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      dialogRefToDisplay.close();
    },
  },
});
</script>
