<template>
  <section class="hero container max-w-screen-lg mx-auto pb-10 flex">

    <img alt="Vue logo" src="./assets/dataland-logo-lr.png" class="mx-auto" >
  </section>
  <HelloWorld msg="Welcome to DataLand"/>
  <APIClient/>

</template>

<script>
import HelloWorld from './components/HelloWorld.vue'
import APIClient from "@/components/APIClient";


export default {
  name: 'App',
  components: {
    HelloWorld,
    APIClient
  }
}
</script>

<style>
/*@import 'https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css';*/
/*@import 'https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css';*/
/*@import 'https://cdnjs.cloudflare.com/ajax/libs/bulma/0.9.3/css/bulma.min.css';*/
@import 'https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css';
@import './assets/css/main.css';
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

</style>
