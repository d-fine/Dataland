<template>
  <div
    style="
      font-size: var(--font-size-xl);
      font-weight: var(--font-weight-bold);
      margin: var(--spacing-xxl) var(--spacing-none);
    "
  >
    Redirecting...
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';

const { register } = defineProps<{ register?: boolean }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

/**
 * Handles the authentication redirect logic for Keycloak login/register flows.
 * Redirects the user based on authentication state and session storage flags.
 * @returns {Promise<void>} Resolves when the redirect logic is complete.
 */
async function handleAuthRedirect(): Promise<void> {
  const redirectKey = register ? 'dataland_register_redirect_pending' : 'dataland_login_redirect_pending';

  const platformRedirectUri = `${globalThis.location.origin}/platform-redirect`;
  const astroHomeUri = `${globalThis.location.origin}/`;

  const keycloak = await assertDefined(getKeycloakPromise)();

  if (keycloak.authenticated) {
    globalThis.location.replace(platformRedirectUri);
    return;
  }

  const wasPending = sessionStorage.getItem(redirectKey);
  if (wasPending) {
    sessionStorage.removeItem(redirectKey);
    globalThis.location.replace(astroHomeUri);
    return;
  }

  sessionStorage.setItem(redirectKey, 'true');
  const action = register
    ? keycloak.register({ redirectUri: platformRedirectUri })
    : keycloak.login({ redirectUri: platformRedirectUri });

  action.catch((error) => console.error(error));
}

/**
 * Handles the 'pageshow' event for page transitions.
 * If the event is persisted, re-runs the authentication redirect logic.
 * @param {PageTransitionEvent} event - The page transition event.
 * @returns {void}
 */
let pageShowHandler: ((event: PageTransitionEvent) => void) | null = null;

onMounted((): void => {
  void handleAuthRedirect();

  pageShowHandler = (event: PageTransitionEvent): void => {
    if (event.persisted) {
      void handleAuthRedirect();
    }
  };

  window.addEventListener('pageshow', pageShowHandler);
});

onBeforeUnmount((): void => {
  if (pageShowHandler) {
    window.removeEventListener('pageshow', pageShowHandler);
  }
});
</script>
