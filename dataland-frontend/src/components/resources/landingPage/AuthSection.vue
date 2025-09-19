<template>
  <template v-if="isLandingPage">
    <div v-if="isUserLoggedIn == true" data-test="backToPlatformLink">
      <Button
        variant="text"
        severity="secondary"
        label="BACK TO PLATFORM"
        @click="backToPlatform"
        icon="pi pi-arrow-right"
        iconPos="right"
      />
    </div>

    <div v-if="isUserLoggedIn == false" class="header__authsection">
      <Button
        label="LOGIN"
        data-test="login-dataland-button"
        @click="login"
        icon="pi pi-user"
        variant="text"
        severity="secondary"
      />
      <Button label="SIGN UP" data-test="signup-dataland-button" @click="register" rounded />
    </div>
  </template>
  <template v-else>
    <div class="header__authsection">
      <Button label="Log in" data-test="login-dataland-button" @click="login" variant="outlined" />
      <Button label="SIGN UP" data-test="signup-dataland-button" @click="register" />
    </div>
  </template>
</template>

<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { useRouter } from 'vue-router';
import { loginAndRedirectToRedirectPage, registerAndRedirectToSearchPage } from '@/utils/KeycloakUtils';
import Button from 'primevue/button';

const router = useRouter();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const isUserLoggedIn = ref<undefined | boolean>(undefined);
const { isLandingPage } = defineProps<{
  isLandingPage: boolean;
}>();
/**
 * Sends the user to the keycloak login page (if not authenticated already)
 */
const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      if (window.location.pathname == '/') {
        void loginAndRedirectToRedirectPage(keycloak);
      } else {
        keycloak.login().catch((error) => console.error(error));
      }
    })
    .catch((error) => console.error(error));
};

/**
 * Sends the user back to the platform
 */
const backToPlatform = (): void => {
  void router.push({ path: '/platform-redirect' });
};

onMounted(() => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      isUserLoggedIn.value = keycloak.authenticated;
    })
    .catch((error) => console.error(error));
});

/**
 * Sends the user to the keycloak register page (if not authenticated) and logs in otherwise
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      if (window.location.pathname == '/') {
        void registerAndRedirectToSearchPage(keycloak);
      } else {
        keycloak.register().catch((error) => console.error(error));
      }
    })
    .catch((error) => console.error(error));
};
</script>
<style scoped lang="scss">
@media only screen and (max-width: 768px) {
  .header {
    background-color: var(--p-pink-300);
    padding: 16px;
    margin: 0;
    width: 100%;
    border-radius: 0;
  }
}
</style>
