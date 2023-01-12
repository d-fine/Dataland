<template>
  <slot v-if="authenticated"></slot>
  <div v-else class="h-screen w-full relative">
    <div class="d-center-div">
      <h1 class="text-justify text-base font-normal">
        Checking Log-In status.
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </h1>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

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
      assertDefined(this.getKeycloakPromise)()
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
