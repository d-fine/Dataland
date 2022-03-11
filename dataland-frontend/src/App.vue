<template>
  <div id="app">
    <SubscriptionForm/>
    <SkyminderForm/>
  </div>
</template>

<script>
import SubscriptionForm from "@/components/SubscriptionForm";
import SkyminderForm from "@/components/forms/SkyminderForm";

export default {
  name: 'app',
  components: {
    SubscriptionForm,
    SkyminderForm
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
