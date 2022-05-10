<template>
  <div class="surface-ground">
    <div class="grid align-items-center">
      <div class="col-6 text-left">
        <router-link to="/">
          <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2"/>
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
      <div class="col-4 col-offset-2">
        <router-link to="/searchtaxonomy">
          <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto">
        </router-link>
      </div>
    </div>
    <div class="grid">
      <div class="col-10 col-offset-2 pb-0">
        <p class="uppercase mb-0">Built by</p>
      </div>
      <div class="col-1 col-offset-2 pt-0">
        <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
      </div>
    </div>
    <div class="grid">
      <div class="col-offset-2 col-10">
        <img src="@/assets/images/logos/pwc_logo_black_white.svg" alt="pwc" class="small-logo pr-3"/>
        <img src="@/assets/images/logos/dfine_logo.svg" alt="d-fine GmbH" class="small-logo pr-3"/>
        <img src="@/assets/images/logos/quentic_logo.png" alt="quentic" class="small-logo"/>

      </div>
    </div>
  </div>
  <GridHelper/>
</template>

<script>
import GridHelper from "@/components/helper/GridHelper";
export default {
  name: "LandingLogin",
  components: {GridHelper}

}
</script>
<style>
.small-logo {
  height: 3em
}
</style>