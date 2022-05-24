<template>
    <router-view />
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
body {
  margin: unset;
}
</style>