<template>
  <div v-if="!authenticated" class="h-screen w-full relative">
    <div class="d-center-div">
      <h1 class="text-justify text-base font-normal">
        Checking Log-In status.
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </h1>
    </div>
  </div>
  <slot v-if="authenticated"></slot>
</template>

<style scoped>
.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}
</style>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "AuthenticationWrapper",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },
  mounted: function () {
    if (!this.authenticated) {
      this.getKeycloakPromise?.()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            return keycloak.login();
          }
        })
        .catch((error) => console.log(error));
    }
  },
});
</script>
