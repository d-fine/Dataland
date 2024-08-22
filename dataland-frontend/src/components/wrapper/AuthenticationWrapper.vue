<template>
  <MobileWarningPage v-if="isUserOnMobileDevice" />
  <template v-else>
    <slot v-if="authenticated || disableAuthenticationWrapper"></slot>
    <MiddleCenterDiv v-else>
      <h1 class="text-justify text-base font-normal">
        Checking Log-In status.
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </h1>
    </MiddleCenterDiv>
  </template>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import MiddleCenterDiv from '@/components/wrapper/MiddleCenterDivWrapper.vue';
import MobileWarningPage from '@/components/pages/MobileWarningPage.vue';

export default defineComponent({
  name: 'AuthenticationWrapper',
  components: { MiddleCenterDiv, MobileWarningPage },
  props: {
    disableAuthenticationWrapper: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    isUserOnMobileDevice(): boolean {
      const userAgent = window.navigator.userAgent.toLowerCase();
      return /iphone|ipad|ipod|android|blackberry|mini|windows\sce|palm/i.test(userAgent);
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      authenticated: inject<boolean>('authenticated'),
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
