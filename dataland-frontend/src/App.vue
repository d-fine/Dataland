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
@import 'https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css';
@import './assets/css/main.css';


</style>
