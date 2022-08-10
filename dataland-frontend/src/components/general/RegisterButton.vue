<template>
  <div class="col-12">
    <PrimeButton
      class="uppercase p-button pl-1 pr-1 pb-1 pt-1 justify-content-center h-3rem w-full"
      name="join_dataland_button"
      @click="register"
    >
      <span class="d-letters d-button"> Join Dataland </span>
      <i class="material-icons pl-1" aria-hidden="true" alt="chevron_right">chevron_right</i>
    </PrimeButton>
  </div>
</template>

<script>
import PrimeButton from "primevue/button";

export default {
  name: "RegisterButton",
  components: { PrimeButton },
  inject: ["authenticated", "getKeycloakPromise"],
  methods: {
    register() {
      this.getKeycloakPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const url = keycloak.createRegisterUrl({ redirectUri: "/search/eutaxonomy" });
            location.assign(url);
          }
        })
        .catch((error) => console.log("error: " + error));
    },
  },
};
</script>
