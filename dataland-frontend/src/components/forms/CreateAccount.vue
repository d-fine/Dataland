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
        <pre>{{email}}</pre>
        <pre>{{password}}</pre>
        <FormKit
            v-model="model"
            submit-label="JOIN NOW"
            :submit-attrs="{
                    'name': 'postEUData'
                  }"
            type="form"
            id="createEuTaxonomyForm"
            @submit="authenticate">
          <FormKit
              type="email"
              name="email"
              placeholder="Valid email address"
              :inner-class="innerClass"
              :input-class="inputClass"
              validation="required|email"
              validation-messages="Please provide a valid email address."
          >
            <template #input>
              <span class="p-float-label">
                  <InputText id="email" type="text" v-model="email"/>
                  <label for="email">Email</label>
              </span>

            </template>
          </FormKit>
          <FormKit
              type="password"
              name="password"
              placeholder="Password"
              :inner-class="innerClass"
              :input-class="inputClass"
              :outer-class="outerClass"
              :wrapper-class="wrapperClass"
              validation="required|length:8"
              validation-messages="Please provide a password that is at least 8 characters long."
          >
            <template #input>
              <span class="p-float-label">
                  <InputText id="password" type="text" v-model="password"/>
                  <label for="password">InputText</label>
              </span>

            </template>
          </FormKit>

        </FormKit>


      </div>
    </div>
  </MarginWrapper>

</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import {FormKit} from "@formkit/vue"
import InputText from "primevue/inputtext";
import {authenticate} from "@/utils/keycloak";
export default {
  name: "CreateAccount",
  components: {MarginWrapper, FormKit, InputText},
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