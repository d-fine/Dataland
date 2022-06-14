<template>
  <MarginWrapper>
    <div class="grid col-12 lg:pt-8">
      <iframe name="keycloak-iframe" :src="url" class="col-12 lg:pt-8 ">
      </iframe>
    </div>
  </MarginWrapper>
</template>

<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "KeyCloakIframe",
  components: {MarginWrapper},
  props: {
    url: {
      type: String
    }
  },
}
</script>
