<template>
  <div class="col-12" >
    <Button v-if="!loggedIn" label="Join" class="d-letters d-button uppercase p-button p-button-sm justify-content-center w-5rem" name="join_dataland_button" @click="openRegister" />
    <Dialog v-model:visible="displayRegister" class="col-4 lg:pt-8" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/register"/> </p>
    </Dialog>
    <Button v-if="!loggedIn" label="Login" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="openLogin" />
    <Dialog v-model:visible="displayLogin" class="col-4 lg:pt-8" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/login"/> </p>
    </Dialog>
    <Button v-if="loggedIn" label="Logout" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="openLogout" />
    <Dialog v-model:visible="displayLogout" class="col-10 lg:pt-8" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/logout"/> </p>
    </Dialog>
  </div>
</template>
<script>

import Dialog from 'primevue/dialog';
import KeyCloakIframe from "@/components/forms/KeyCloakIframe";
import Button from "primevue/button";
import Keycloak from "keycloak-js";

export default {
  name: 'UserAuthenticationButtons',
  components: {KeyCloakIframe, Dialog, Button},
  created() {
    window.onmessage = message => {
      if (message.data === 'DatalandLoginSucceded') {
        this.displayLogin=false;
        this.attemptLoginWithSessionStoredData()
      }
      else if (message.data === 'DatalandLogoutSucceded') {
        this.displayLogout=false;
        this.clearLogin()
      }
    };
    this.attemptLoginWithSessionStoredData();
  },
  methods: {
    openRegister() {
      this.displayRegister = true;
    },
    openLogin() {
      this.displayLogin = true;
    },
    openLogout() {
      this.displayLogout = true;
    },
    attemptLoginWithSessionStoredData() {
      const access_token = window.sessionStorage.getItem("keycloakAccessToken");
      const refresh_token = window.sessionStorage.getItem("keycloakRefreshToken");
      if (!access_token || !refresh_token) {
        this.clearLogin();
      }
      else {
        const initOptions = {
          realm: "datalandsecurity",
          url: "/keycloak",
          clientId: "dataland-public",
        }
        const keycloak = new Keycloak(initOptions);
        keycloak.init({
          token: access_token, refreshToken: refresh_token
        }).then((auth) => {
          if(auth) {
            this.loggedIn=true;
            setInterval(() => {
              keycloak.updateToken(70).then((refreshed) => {
                if (refreshed) {
                  console.log('Token refreshed')
                  window.sessionStorage.setItem("keycloakAccessToken", keycloak.token)
                  window.sessionStorage.setItem("keycloakRefreshToken", keycloak.refreshToken)
                } else {
                  console.log('Token not refreshed')
                }
              }).catch(() => {
                console.error('Failed to refresh token');
                this.clearLogin()
              });
            }, 6000)
          }
          else {
            this.clearLogin()
          }
        }).catch(()=>{
          console.error('Failed to init keycloak');
          this.clearLogin()
        });
      }
    },
    clearLogin() {
      window.sessionStorage.removeItem("keycloakAccessToken");
      window.sessionStorage.removeItem("keycloakRefreshToken");
      this.loggedIn = false;
    }
  },
  data() {
    return {
      displayRegister: false,
      displayLogin: false,
      displayLogout: false,
      loggedIn: false,
      keycloak: null
    }
  }
}
</script>