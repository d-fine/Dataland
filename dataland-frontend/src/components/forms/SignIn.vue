<template>
  <MarginWrapper>
    <div class="grid">
        <div class="col-12 text-center">
      <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-3"/>

      <h1 >
        Login
      </h1>
        </div>
    </div>
    <div class="p-fluid grid">



        <div class="field col-12 md:col-12">
                    <div class="p-float-label">
                        <InputText id="inputtext" type="text" v-model="value1" />
                        <label for="inputtext">InputText</label>
                    </div>
        </div>
      <div class="field col-12 md:col-12">
        <FormKit
            v-model="model"
            submit-label="JOIN NOW"
            :submit-attrs="{
                    'name': 'postEUData'
                  }"
            type="form"
            id="signin"
            :form-class="formClass"
            @submit="postEUData">

                        <FormKit
                            id="emailfield"
                            type="email"
                            name="email"
                            label="email"
                            :inner-class="innerClass"
                            :outer-class="outerClass"
                            :wrapper-class="wrapperClass"
                            :input-class="inputClass"
                            :sections-schema="{

                            }"
                            validation="required|email"
                            validation-messages="Please provide a valid email address."
                        >
                        </FormKit>
        </FormKit>
        </div>

    </div>
  </MarginWrapper>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import InputText from "primevue/inputtext";
import {FormKit} from "@formkit/vue"
export default {
  name: "SignIn",
  components: {MarginWrapper, InputText, FormKit},
  data(){
    return {
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
        'formkit-input':false,
        'p-inputtext': true,
        'p-component': true
      },
      formClass: {
        'formkit-form':false
      }
    }
  }
}
</script>

<style scoped>

</style>