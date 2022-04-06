<template>
  <div class="grid surface-900 align-items-center">
    <div class="col-1">
    <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" />

    </div>
    <div class="col-1 col-offset-10">
      <i class="material-icons text-white" aria-hidden="true">help_outline</i>
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