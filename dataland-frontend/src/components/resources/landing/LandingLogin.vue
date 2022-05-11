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
      <div class="col-offset-2 col-10" text-align-left>
        <img src="@/assets/images/logos/pwc.svg" alt="pwc" class="small-logo pr-3"/>
        <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="small-logo pr-3"/>
        <img src="@/assets/images/logos/quentic.svg" alt="quentic" class="small-logo"/>

      </div>
    </div>
    <div class="grid">
      <div class="col-6 col-offset-4">
      <Card class="d-card">
        <template #title>
          <h2 style = 'color: #013D48'>Join Dataland to access our data</h2>
        </template>
        <template #content>
          <p style = 'color:#958D7C'>Register free to access <b>EU Taxonomy</b> data from 450 Germany public companies and Sustainability Data from
            <b>+100 German SMEs</b>. Be notified about updates</p>
          <div class="grid align-items-end">
            <div class = "pl-2">
              <input v-model="message" placeholder="Email address" class="box-area">
            </div>
            <div >
              <Button label="Get Started" class="uppercase p-button">Get Started
                <i class="material-icons pl-3" aria-hidden="true">arrow_forward_ios</i>
              </Button>
            </div>
          </div>
          <p>
            <router-link to="linkedin.de">
              <a style = 'color:#E67F3F'>Join with Linkedin</a>
           </router-link>
          </p>

        </template>
      </Card>

      </div>
    </div>
  <GridHelper/>
  </div>
</template>

<script>
import GridHelper from "@/components/helper/GridHelper";
import Card from "primevue/card";
export default {
  name: "LandingLogin",
  components: {GridHelper, Card}

}
</script>
<style>
.small-logo {
  /*height: 3em*/
}
.box-area{
  height: 3.2em
}
</style>