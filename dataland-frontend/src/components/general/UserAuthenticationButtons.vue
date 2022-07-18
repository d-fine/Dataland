<template>
  <div class="col-12">
    <PrimeButton
      v-if="!authenticated"
      label="Join"
      class="d-letters d-button uppercase p-button p-button-sm justify-content-center w-5rem"
      name="join_dataland_button"
      @click="register"
    />
    <PrimeButton
      v-if="!authenticated"
      label="Login"
      class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4"
      name="login_dataland_button"
      @click="login"
    />
    <PrimeButton
      v-if="authenticated"
      label="Logout"
      class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4"
      name="logout_dataland_button"
      @click="logout"
    />
  </div>
</template>
<script>
import PrimeButton from "primevue/button";

export default {
  name: "UserAuthenticationButtons",
  components: { PrimeButton },
  inject: ["authenticated", "getKeycloakInitPromise"],
  methods: {
    register() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const url = keycloak.createRegisterUrl();
            location.assign(url);
          }
        })
        .catch((error) => console.log("error: " + error));
    },
    login() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const url = keycloak.createLoginUrl();
            location.assign(url);
          }
        })
        .catch((error) => console.log("error: " + error));
    },
    logout() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            keycloak.logout();
          }
        })
        .catch((error) => console.log("error: " + error));
    },
  },
};
</script>
