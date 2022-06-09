<template>
  <MarginWrapper>
    <iframe src="http://www.d-fine.de" class="col-8" ></iframe>
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
