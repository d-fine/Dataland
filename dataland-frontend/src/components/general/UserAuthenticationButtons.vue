<template>
  <div class="col-12" >
    <Button v-if="!authenticated" label="Join" class="d-letters d-button uppercase p-button p-button-sm justify-content-center w-5rem" name="join_dataland_button" @click="openRegister" />
    <Dialog v-model:visible="displayRegister" class="col-4 lg:pt-0" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/register"/> </p>
    </Dialog>
    <Button v-if="!authenticated" label="Login" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="openLogin" />
    <Dialog v-model:visible="displayLogin" class="col-4 lg:pt-0" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/login"/> </p>
    </Dialog>
    <Button v-if="authenticated" label="Logout" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="openLogout" />
    <Dialog v-model:visible="displayLogout" class="col-10 lg:pt-8" :modal="true" :showHeader="false" :dismissableMask="true">
      <p class="m-0"> <KeyCloakIframe url="/logout"/> </p>
    </Dialog>
  </div>
</template>
<script>

import Dialog from 'primevue/dialog';
import KeyCloakIframe from "@/components/forms/KeyCloakIframe";
import Button from "primevue/button";

export default {
  name: 'UserAuthenticationButtons',
  components: {KeyCloakIframe, Dialog, Button},
  created() {
    window.onmessage = message => {
      if (message.data === 'DatalandToggleLogin') {
        this.displayLogin=false;
        this.displayLogout=false;
        this.displayRegister=false;
        this.keycloak_init();
      }
    };
  },
  inject: ['authenticated', 'keycloak_init'],
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
  },
  data() {
    return {
      displayRegister: false,
      displayLogin: false,
      displayLogout: false,
    }
  }
}
</script>