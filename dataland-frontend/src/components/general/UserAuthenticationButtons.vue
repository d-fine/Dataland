<template>
  <div class="col-12">
    <PrimeButton
      label="Login"
      class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4"
      name="login_dataland_button"
      @click="login"
    />
  </div>
</template>
<script>
import PrimeButton from "primevue/button";

export default {
  name: "UserAuthenticationButtons",
  components: { PrimeButton },
  inject: ["getKeycloakPromise"],
  methods: {
    login() {
      this.getKeycloakPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = keycloak.createLoginUrl({
              redirectUri: `${baseUrl}/companies`,
            });
            location.assign(url);
          }
        })
        .catch((error) => console.log("error: " + error));
    },
  },
};
</script>
