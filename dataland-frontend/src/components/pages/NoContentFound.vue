<template>
  <TheHeader :showUserProfileDropdown="false" />
  <TheContent>
    <h2>Sorry an error occurred!</h2>
    <p>Whoops! Something went wrong!</p>
    <p>
      Go back to
      <router-link to="/" class="text-primary" title="back to landing page"
        >Dataland</router-link
      >
    </p>
  </TheContent>
</template>

<script>
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
export default {
  name: "NoContentFound",
  components: { TheContent, TheHeader },
};
</script>
