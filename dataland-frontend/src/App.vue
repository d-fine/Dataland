<template>
  <div>
    <NavbarTop />
    <br>
    <router-view />

  </div>
</template>

<script>
import NavbarTop from "@/components/menus/NavbarTop";




export default {
  name: 'app',
  components: {
    NavbarTop
  }
}
</script>

<style lang="scss">
@import "./assets/css/main.css";
@import "./assets/css/forms.css";
@import "./assets/css/variables";

</style>