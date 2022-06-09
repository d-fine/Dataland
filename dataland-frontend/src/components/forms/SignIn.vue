<template>
  <MarginWrapper>
    <div class="grid col-12 lg:pt-8">
      <iframe src="https://www.d-fine.de" class="col-12 lg:pt-8 ">
      </iframe>
    </div>
  </MarginWrapper>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import {authenticate} from "@/utils/keycloak";

export default {
  name: "SignIn",
  components: {MarginWrapper},
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
