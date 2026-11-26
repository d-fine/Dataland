<template>
  <MobileWarningPage v-if="isUserOnMobileDevice && !disableAuthenticationWrapper" />
  <template v-else>
    <slot v-if="isAuthenticated || disableAuthenticationWrapper"></slot>
    <MiddleCenterDiv v-else>
      <h1 class="text-justify text-base font-normal">
        Checking Log-In status.
        <DatalandProgressSpinner />
      </h1>
    </MiddleCenterDiv>
  </template>
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import MiddleCenterDiv from '@/components/wrapper/MiddleCenterDivWrapper.vue';
import MobileWarningPage from '@/components/pages/MobileWarningPage.vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { computed, inject, toValue, watch, type MaybeRef } from 'vue';

/**
 * Renders its content only for logged-in users and redirects anonymous users to the login page.
 * If `disableAuthenticationWrapper` is set, the content is rendered for everyone. The wrapper is meant to stay mounted
 * while the prop changes with the route, so that its content is not remounted when switching between public and
 * protected pages.
 */
const props = withDefaults(defineProps<{ disableAuthenticationWrapper?: boolean }>(), {
  disableAuthenticationWrapper: false,
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const authenticated = inject<MaybeRef<boolean | undefined>>('authenticated');
const isAuthenticated = computed(() => toValue(authenticated) ?? false);

const isUserOnMobileDevice = /iphone|ipad|ipod|android|blackberry|mini|windows\sce|palm/i.test(
  globalThis.navigator.userAgent.toLowerCase()
);

/**
 * Redirects to the login page if Keycloak reports that the user is not logged in.
 */
function redirectToLoginIfNotAuthenticated(): void {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        return keycloak.login();
      }
    })
    .catch((error) => console.log(error));
}

watch(
  () => !isAuthenticated.value && !props.disableAuthenticationWrapper,
  (isLoginRequired) => {
    if (isLoginRequired) redirectToLoginIfNotAuthenticated();
  },
  { immediate: true }
);
</script>
