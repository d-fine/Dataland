<!--
This component is intended to be used inside an iframe as the only component loaded.
It will post a message to the outer window stating "DatalandLoginSucceeded" after login succeded. It will store
keycloakAccessToken and keycloakRefreshToken in window session store.
-->
<template>
<div/>
</template>

<script>

import Keycloak from "keycloak-js";

export default {
  name: "LogoutIframeContent",
  created() {
    const initOptions = {
      realm: "datalandsecurity",
      url: "/keycloak",
      clientId: "dataland-public",
      onLoad: 'login-required'
    }
    const keycloak = new Keycloak(initOptions);
    console.log("init kc")
    keycloak.init({onLoad: "login-required"}).then(authenticated => {
      if (authenticated) {
        console.log("authenticated - trying to log out")
        keycloak.logout()
      }
    }).then(()=> {
      console.log("logged out")
      window.sessionStorage.removeItem('keycloakAccessToken')
      window.sessionStorage.removeItem('keycloakRefreshToken')
      window.top.postMessage('DatalandLogoutSucceded', '*')
    })
  },
}
</script>