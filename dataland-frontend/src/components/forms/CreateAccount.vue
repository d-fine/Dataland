<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-12 text-center">
        <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-3"/>
        <h1>
          Create an account
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
                          <InputText id="password" type="text" v-model="password"/>
                          <label for="password">Password</label>
                      </span>
          </div>
        </div>

        <Button class="p-button" @click="authenticate">Create</Button>
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
  name: "CreateAccount",
  components: {MarginWrapper,  InputText, Button},
  methods: {
    authenticate() {
      authenticate()
    }
  },
  data() {
    return {
      model: null,
      value1: null,
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