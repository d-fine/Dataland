<template>
  <slot v-if="authenticated || disableAuthenticationWrapper"></slot>
  <MiddleCenterDiv v-else>
    <h1 class="text-justify text-base font-normal">
      Checking Log-In status.
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </h1>
  </MiddleCenterDiv>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent({
  name: "AuthenticationWrapper",
  components: { MiddleCenterDiv },
  props: {
    disableAuthenticationWrapper: {
      type: Boolean,
      default: false,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },
  mounted: function () {
    if (!this.authenticated && !this.disableAuthenticationWrapper) {
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
