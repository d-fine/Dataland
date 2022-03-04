<template>
  <section class="hero container max-w-screen-lg mx-auto pb-10 flex">

    <img alt="Dataland logo" src="./assets/images/logos/dataland-logo-lr.png" class="mx-auto" >
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
    APIClient,
  },
  created(){
    console.log("Node Env:" + process.env.NODE_ENV)
    console.log("base URL:" + process.env.VUE_APP_URL)
    console.log("api URL is: " + `${process.env.VUE_APP_URL}/api`)
  }
}
</script>

<style>
@import './assets/css/main.css';


</style>
