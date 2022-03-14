<template>
  <div>
    <NavbarTop />
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

<style>
@import "./assets/css/main.css";
@import "./assets/css/forms.css";

</style>