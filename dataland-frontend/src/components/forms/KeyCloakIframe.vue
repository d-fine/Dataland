<template>
  <MarginWrapper>
    <div class="grid col-12 lg:pt-8">
      <iframe v-if="showLoginIframe" :src="url" class="col-12 lg:pt-8 ">
      </iframe>
    </div>
  </MarginWrapper>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import {authenticate} from "@/utils/keycloak";

export default {
  name: "KeyCloakIframe",
  components: {MarginWrapper},
  props: {
    url: {
      type: String
    }
  },
  methods: {
    authenticate() {
      authenticate(this.email, this.password)
    }
  },
  data() {
    return {
      showLoginIframe: true,
    }
  }
}
</script>
