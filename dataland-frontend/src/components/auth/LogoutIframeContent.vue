<!--
This component is intended to be used inside an iframe as the only component loaded.
It will post a message to the outer window stating "DatalandLoginSucceeded" after login succeded. It will store
keycloakAccessToken and keycloakRefreshToken in window session store.
-->
<template>
  <div/>
</template>

<script>

export default {
  name: "LogoutIframeContent",
  created() {
    this.getKeycloakInitPromise().then((keycloak) => {
      if (keycloak.authenticated) {
        console.log("authenticated - trying to log out")
        keycloak.logout()
      } else {
        console.log("logged out")
        window.parent.postMessage('DatalandToggleLogin', '*')
      }
    })
  },
  inject: ['getKeycloakInitPromise'],
}
</script>
