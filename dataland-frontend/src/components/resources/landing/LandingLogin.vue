<template>
  <div class="surface-ground pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0">
        <div class="col-6 text-left">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2"/>
          </router-link>
        </div>
        <div class="col-3 col-offset-3">
          <Button label="Join" class="d-letters d-button uppercase p-button p-button-sm justify-content-center w-5rem" name="join_dataland_button" @click="openRegister" />
          <Dialog v-model:visible="displayRegister" :modal="true">
            <p class="m-0"> <CreateAccount/> </p>
          </Dialog>
            <Button class="uppercase p-button p-button-sm justify-content-center bg-white-alpha-10 w-5rem ml-4" name="login_dataland_button" @click="openLogin">
              <span class="d-letters text-primary d-button">
                Login
              </span>
            </Button>
          <Dialog v-model:visible="displayLogin" :modal="true">
            <p class="m-0"> <SignIn/> </p>
          </Dialog>
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div class="col-4 col-offset-1">
          <h1 class="text-justify">COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE
            COSTS.</h1>
        </div>
        <div class="col-4 col-offset-1">
          <router-link to="/searchtaxonomy">
            <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto">
          </router-link>
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-10 col-offset-1 pb-0">
          <p class="uppercase mb-0">Built by</p>
        </div>
        <div class="col-1 col-offset-1 pt-0">
          <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line"/>
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-offset-1 col-10">
          <div class="grid align-items-baseline">
            <div class="col-1">
              <img src="@/assets/images/logos/pwc.svg" alt="pwc" class="pr-5"/>
            </div>
            <div class="col-1">
              <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="d-small-logo pr-5"/>
            </div>
          </div>
        </div>
      </div>
      <div class="grid m-0">
        <div class="col-6 col-offset-3">
          <Card class="d-card">
            <template #title>
              <h2 class="text-gray-100 text-left">Join Dataland to access our data</h2>
            </template>
            <template #content>
              <p class="text-gray-800 text-left">Register free to access <strong>EU Taxonomy</strong> data from 450
                Germany public
                companies and Sustainability Data from
                <strong>+100 German SMEs</strong>. Be notified about updates</p>
              <div class="grid">
                <div class="col-9 p-fluid pr-0">
                  <InputText type="text" placeholder="Email address" class="h-2rem" name="email_input_landing"/>
                </div>
                <div class="col-3 p-fluid pl-0">
                  <Button class="uppercase p-button p-button-sm pl-2 pr-1 pb-1 pt-1 justify-content-center h-2rem" name="get_started_button">
                    <span class="d-letters d-button">
                      Get Started
                    </span>
                    <i class="material-icons pl-1" aria-hidden="true">chevron_right</i>
                  </Button>
                </div>
              </div>
              <p class="text-primary underline text-left cursor-pointer">
                Join with Linkedin
              </p>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Card from "primevue/card";
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Button from "primevue/button";
import {authenticate} from "@/utils/keycloak";
import CreateAccount from "@/components/forms/CreateAccount";
import SignIn from "@/components/forms/SignIn";

export default {
  name: "LandingLogin",
  components: {SignIn, CreateAccount, Card, Dialog,InputText, Button},
  methods: {
    authenticate() {
      authenticate()
      alert("ran authentication")
    },
    openRegister() {
      this.displayRegister = true;
    },
    openLogin() {
      this.displayLogin = true;
    },
    closeRegister() {
      this.displayRegister = false;
    },
    closeLogin() {
      this.displayLogin = false;
    }
  },
    data() {
      return {
        displayRegister: false,
        displayLogin: false
      }
    }
}
</script>