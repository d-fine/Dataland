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
        return true
      } else {
        return keycloak.register()
      }
    }).then((authenticated) => {
      if (authenticated) {
        window.parent.postMessage('DatalandToggleLogin', location.origin)
      } else {
        window.location.reload();
      }
    }).catch((error) => console.log("error: " + error))
  },
  inject: ["getKeycloakInitPromise"],
}
</script>
