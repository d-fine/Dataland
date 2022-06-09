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
  name: "LoginIframeContent",
  created() {
    const initOptions = {
      realm: "datalandsecurity",
      url: "http://localhost/keycloak",
      clientId: "dataland-public",
      onLoad: 'login-required'
    }
    const keycloak = new Keycloak(initOptions);
    keycloak.init({onLoad: "login-required"}).then(authenticated => {
      if (!authenticated) {
        window.location.reload();
      } else {
        window.sessionStorage.setItem('keycloakAccessToken', keycloak.token)
        window.sessionStorage.setItem('keycloakRefreshToken', keycloak.refreshToken)
        window.top.postMessage('DatalandLoginSucceded', '*')
      }
    })
  },
}
</script>