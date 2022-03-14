<template>
  <div id="app">
    <section class="hero container max-w-screen-lg mx-auto pb-10 flex">
      <img alt="Dataland logo" src="./assets/images/logos/dataland-logo-lr.png" class="mx-auto" >
    </section>
    <WelcomeDataland msg="Welcome to DataLand"/>
    <RetrieveSkyminder/>
    <APIClient/>
  </div>
</template>

<script>
import WelcomeDataland from "@/components/WelcomeDataland";
import APIClient from "@/components/APIClient";
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";




export default {
  name: 'app',
  components: {
    RetrieveSkyminder,
    WelcomeDataland,
    APIClient
  }
}
</script>

<style>
@import "./assets/css/main.css";

</style>