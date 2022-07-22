<template>
  <TheHeader/>
  <TheContent>
    <p>Whoops! Something went wrong!</p>
    <p>Go back to <router-link to="/" class="text-primary">Dataland</router-link></p>
  </TheContent>
</template>

<script>
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
export default {
  name: "NoContentFound",
  components: {TheContent, TheHeader},
};
</script>
