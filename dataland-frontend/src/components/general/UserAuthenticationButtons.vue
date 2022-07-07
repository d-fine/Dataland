<template>
  <div class="col-12" >
    <Button v-if="!authenticated" label="Login" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="login" />
    <Button v-if="authenticated" label="Logout" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="logout_dataland_button" @click="logout" />
  </div>
</template>
<script>

import Button from "primevue/button";

export default {
  name: 'UserAuthenticationButtons',
  components: {Button},
  inject: ['authenticated', 'getKeycloakInitPromise'],
  methods: {
    login() {
      this.getKeycloakInitPromise().then((keycloak) => {
        if (!keycloak.authenticated) {
          return keycloak.login()
        }
      }).catch((error) => console.log("error: " + error))
    },
    logout() {
      this.getKeycloakInitPromise().then((keycloak) => {
        if (keycloak.authenticated) {
          keycloak.logout()
        }
      }).catch((error) => console.log("error: " + error))
    },
  },
}
</script>