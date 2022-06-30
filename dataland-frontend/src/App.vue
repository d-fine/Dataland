<template>
  <router-view/>
</template>

<script>


import Keycloak from "keycloak-js";
import {computed} from 'vue';


export default {
  name: 'app',
  data() {
    return {
      keycloak: null,
      keycloak_init_promise: null,
      keycloak_init_promise_finished: true,
      keycloak_authenticated: null,
    }
  },
  methods: {
    keycloak_init() {
      console.log("keycloak_init")
      if (this.keycloak_init_promise_finished) {
        console.log("keycloak_init promise finished...")
        this.keycloak_init_promise = this.keycloak.init({
          onLoad: 'check-sso'
        }).then((authenticated) => {
          this.keycloak_authenticated = authenticated
          return authenticated
        }).catch((error) => {
          console.log("Error in init keycloak ", error)
          this.keycloak_authenticated = false
        }).then(() => {
          return this.keycloak
        });
        this.keycloak_init_promise.finally(() => {
          this.keycloak_init_promise_finished = true
        })
        this.keycloak_init_promise_finished = false
      }
      return this.keycloak_init_promise
    },
  },
  provide() {
    return {
      keycloak_init: this.keycloak_init,
      getKeycloakInitPromise: () => {
        return this.keycloak_init_promise
      },
      authenticated: computed(() => {
        return this.keycloak_authenticated
      })

    }
  },
  created() {
    const initOptions = {
      realm: "datalandsecurity",
      url: "/keycloak",
      clientId: "dataland-public",
      onLoad: 'login-required'
    }
    this.keycloak = new Keycloak(initOptions);
    this.keycloak_init();
  }
}
</script>

<style lang="scss">

@import "./assets/css/main.css";
@import "./assets/css/variables";

body {
  margin: unset;
}
</style>