<template>
  <div id="app">
    <section class="hero container max-w-screen-lg mx-auto pb-10 flex">
      <img alt="Dataland logo" src="./assets/images/logos/dataland-logo-lr.png" class="mx-auto" >
    </section>
    <WelcomeDataland msg="Welcome to DataLand"/>
    <hr>
    <RetrieveSkyminder/>
    <hr>
    <CreateCompany/>
    <CreateEUTaxonomy/>
    <APIClient/>
  </div>
</template>

<script>
import WelcomeDataland from "@/components/WelcomeDataland";
import APIClient from "@/components/APIClient";
import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder";
import CreateCompany from "@/components/forms/CreateCompany";
import CreateEUTaxonomy from "@/components/forms/CreateEUTaxonomy";




export default {
  name: 'app',
  components: {
    CreateEUTaxonomy,
    RetrieveSkyminder,
    WelcomeDataland,
    APIClient,
    CreateCompany
  }
}
</script>

<style>
@import "./assets/css/main.css";

</style>