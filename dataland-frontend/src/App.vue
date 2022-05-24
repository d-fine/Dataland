<template>
  <div class="d-header grid fixed top-0 col-12 surface-900 align-items-center h-4rem">
    <div class="col-6 text-left">
      <router-link to="/">

    <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" class="pl-3 pt-2"/>
      </router-link>

    </div>
    <div class="col-1 col-offset-5">
      <i class="material-icons text-white" aria-hidden="true" @click="authenticate">help_outline</i>
    </div>
  </div>
  <div class="d-content">
    <br>
    <router-view />
  </div>

</template>

<script>


import {authenticate} from "@/utils/keycloak";
export default {
  name: 'app',
  methods: {
    authenticate(){
      authenticate()
      alert("ran authentication")
    }
  }
}
</script>

<style lang="scss">

@import "./assets/css/main.css";
@import "./assets/css/variables";

</style>