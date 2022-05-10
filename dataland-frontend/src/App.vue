<template>
  <div class="d-content">
    <router-view />
  </div>

</template>

<script>



export default {
  name: 'app',
}
</script>

<style lang="scss">

@import "./assets/css/main.css";
@import "./assets/css/variables";
body {
  margin: unset;
}
</style>