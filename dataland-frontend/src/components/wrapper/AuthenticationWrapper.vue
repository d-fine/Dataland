<template>
  <slot v-if="authenticated"></slot>
  <MiddleCenterDiv v-else>
    <h1 class="text-justify text-base font-normal">
      Checking Log-In status.
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </h1>
  </MiddleCenterDiv>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent({
  name: "AuthenticationWrapper",
  components: { MiddleCenterDiv },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },
  mounted: function () {
    if (!this.authenticated) {
      this.getKeycloakPromise!()
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
