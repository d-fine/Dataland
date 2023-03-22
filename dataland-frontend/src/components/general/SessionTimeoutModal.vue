<template>
  <UserAuthenticationButtons v-if="showLogInButton" />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { UserAuthenticationButtons },
  data() {
    return {
      showLogInButton: false,
    };
  },

  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      showLogInButton: boolean;
    };
    this.showLogInButton = dialogRefData.showLogInButton;
  },
});
</script>
