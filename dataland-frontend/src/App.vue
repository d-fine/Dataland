<template>
  <div class="grid surface-900">
    <div class="col-1">
    <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" />

    </div>

  </div>
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