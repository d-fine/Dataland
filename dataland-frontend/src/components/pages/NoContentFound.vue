<template>
  <TheHeader />
  <TheContent>
    <h2>Sorry an error occurred!</h2>
    <p>Whoops! Something went wrong!</p>
    <p>Go back to <router-link to="/" class="text-primary" title="back to landing page">Dataland</router-link></p>
  </TheContent>
</template>

<script>
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
export default {
  name: "NoContentFound",
  components: { TheContent, TheHeader },
};
</script>
