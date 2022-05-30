<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-12 text-center">
        <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-3"/>
        <h1>
          Login
        </h1>
      </div>
    </div>
    <div class="grid flex justify-content-center flex-wrap">
      <div class="col-12 p-fluid align-items-center justify-content-center">
        <div class="grid p-fluid">
          <div class="field col-12">
              <span class="p-float-label">
                  <InputText id="email" type="text" v-model="email"/>
                  <label for="email">Email</label>
              </span>
          </div>
          <div class="field col-12 ">
              <span class="p-float-label">
                  <InputText id="password" type="password" v-model="password"/>
                  <label for="password">Password</label>
              </span>
          </div>
        </div>

        <Button class="p-button uppercase d-letters" @click="authenticate">Sign-In</Button>
      </div>
        <p/>
        <div class="col-4 pt-3">
          Don't have an account?
        </div>
        <div class="col-2">
         <Button label="Join" class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem" name="login_dataland_button" @click="openLogin" />
        </div>

    </div>
  </MarginWrapper>

</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import InputText from "primevue/inputtext";
import Button from "primevue/button";
import {authenticate} from "@/utils/keycloak";

export default {
  name: "SignIn",
  components: {MarginWrapper, InputText, Button},
  methods: {
    authenticate() {
      authenticate(this.email, this.password)
    }
  },
  data() {
    return {
      email: null,
      password: null,
      innerClass: {
        'formkit-inner': false,
      },
      outerClass: {
        'formkit-outer': false,
      },
      wrapperClass: {
        'formkit-wrapper': false,
      },
      inputClass: {
        'formkit-input': false,
        'p-inputtext': true,
        'p-component': true
      },
      formClass: {
        'formkit-form': false
      }
    }
  }
}
</script>

<style scoped>

</style>