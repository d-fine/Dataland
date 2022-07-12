<template>
  <slot v-if="authenticated"></slot>
</template>

<script>
export default {
  name: "AuthenticationWrapper",
  inject: ["authenticated"],
  watch: {
    authenticated(newAuthenticated) {
      if (!newAuthenticated && newAuthenticated !== null) {
        this.$router.push("/");
      }
    },
  },
};
</script>
