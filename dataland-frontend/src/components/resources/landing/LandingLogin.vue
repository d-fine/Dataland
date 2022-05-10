<template>
  <div class="surface-ground">
    <div class="grid align-items-center">
      <div class="col-6 text-left">
        <router-link to="/">
          <img src="@/assets/images/logos/logo_dataland_long.svg" alt="image" class="pl-3 pt-2"/>
        </router-link>
      </div>
      <div class="col-1 col-offset-5">
        <i class="material-icons text-white" aria-hidden="true">help_outline</i>
      </div>
    </div>
    <div class="grid align-items-center">
      <div class="col-4 col-offset-2">
        <h1 class="text-justify">COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE
          COSTS.</h1>
      </div>
      <div class="col-2 col-offset-2">
        <router-link to="/searchtaxonomy">
          <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto">
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "LandingLogin"
}
</script>

<style scoped>

</style>