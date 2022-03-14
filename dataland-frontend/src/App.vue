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
@import "./assets/css/main.css";

</style>