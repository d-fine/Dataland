<template>
  <PrimeButton
    label="Login to preview account"
    class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
    name="login_dataland_button"
    @click="login"
  />
  <!-- <JoinDatalandButton /> -->

  <!--   <div v-if="landingPageData">
    <div v-for="section in landingPageData.sections" :key="section.title">
      <h2>{{ section.title }}</h2>
      <p v-html="section.text"></p>
      <img :src="section.image" :alt="section.title" />
    </div>
  </div> -->
</template>

<script setup lang="ts">
// import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";
import PrimeButton from "primevue/button";
import { inject } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { loginAndRedirectToSearchPage } from "@/utils/KeycloakUtils";
import type Keycloak from "keycloak-js";
/* import { type Page } from "@/types/ContentTypes";
 */
/* defineProps<{ landingPageData: Page | undefined }>();
 */
const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

/**
 * Sends the user to the keycloak login page (if not authenticated already)
 */
const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        loginAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>
<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
}
</style>
