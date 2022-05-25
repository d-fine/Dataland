<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-wrapper">
        <div class="modal-container">
          <div class="modal-body">
            <slot name="body">
              <MarginWrapper>
                <div class="grid">
                  <div class="col-12 text-center">
                    <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-3"/>

                    <h1 >
                      Create an account
                    </h1>
                  </div>
                </div>
                <div class="grid flex justify-content-center flex-wrap">


                  <div class="col-12 flex align-items-center justify-content-center">

                    <FormKit
                        v-model="model"
                        submit-label="JOIN NOW"
                        :submit-attrs="{
                    'name': 'postEUData'
                  }"
                        type="form"
                        id="createEuTaxonomyForm"
                        @submit="postEUData">
                      <FormKit
                          type="email"
                          name="email"
                          label="Enter your email address"
                          placeholder="Valid email address"
                          :inner-class="innerClass"
                          :input-class="inputClass"
                          validation="required|email"
                          validation-messages="Please provide a valid email address."
                      />
                      <FormKit
                          type="password"
                          name="password"
                          label="Password"
                          placeholder="Password"
                          :inner-class="innerClass"
                          :input-class="inputClass"
                          validation="required|length:8"
                          validation-messages="Please provide a password that is at least 8 characters long."
                      />

                    </FormKit>

                  </div>
                </div>
              </MarginWrapper>
            </slot>
          </div>
          <div class="modal-footer">
            <slot name="footer">

            </slot>
          </div>
        </div>
      </div>
    </div>
  </transition>

</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import {FormKit} from "@formkit/vue"

export default {
  name: "CreateAccount",
  components: {MarginWrapper, FormKit},
  data(){
    return {
      innerClass: {
        'formkit-inner': false,
        'p-inputwrapper': true
      },
      inputClass: {
        'formkit-input':false,
        'p-inputtext': true,
        'w-full': true
      },
    }
  }
}
</script>

<style scoped>

</style>