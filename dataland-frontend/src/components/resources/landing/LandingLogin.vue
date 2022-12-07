<template>
  <div class="surface-ground pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0 bg-gray-800">
        <div class="col-9 col-offset-1">
          <p class="text-white text-left text-xl pl-3 pr-3">
            This preview page is intended to give a first impression of Dataland's goals and has no further
            functionality. Dataland is currently being founded and has therefore not yet started operations.
          </p>
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div class="col-5 text-left">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2" />
          </router-link>
        </div>
        <div class="col-2 col-offset-4">
          <UserAuthenticationButtons />
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div class="col-5 col-offset-1">
          <h1 class="text-justify text-6xl font-normal">
            COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE COSTS.
          </h1>
        </div>
        <div class="col-4 col-offset-1">
          <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto" />
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-10 col-offset-1 pb-0">
          <p class="uppercase mb-0">Built by</p>
        </div>
        <div class="col-1 col-offset-1 pt-0">
          <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-offset-1 col-10">
          <div class="grid align-items-baseline">
            <div class="col-1">
              <img src="@/assets/images/logos/pwc.svg" alt="pwc" class="pr-5" />
            </div>
            <div class="col-1">
              <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="d-small-logo pr-5" />
            </div>
          </div>
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-10 col-offset-1 pb-0">
          <p class="uppercase mb-0">Supported by</p>
        </div>
        <div class="col-1 col-offset-1 pt-0">
          <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-offset-1 col-10">
          <div class="grid align-items-baseline">
            <div class="col-1">
              <img src="@/assets/images/logos/t_systems_logo.svg" alt="T-Systems" class="pr-5" height="25" />
            </div>
          </div>
        </div>
      </div>
      <div class="grid m-0">
        <div class="col-4 col-offset-4">
          <Card class="d-card">
            <template #title>
              <p class="text-left text-xxl pl-3 pr-3">Create a preview account to try Dataland</p>
            </template>
            <template #content>
              <p class="d-text-register text-left text-xl pl-3 pr-3">
                Create a preview account to <strong>try Dataland</strong> and access data for
                <strong>EU Taxonomy</strong> and other frameworks.
              </p>
              <div class="grid">
                <div class="col-12 p-fluid pl-3 pr-3">
                  <JoinDatalandButton />
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import Card from "primevue/card";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "LandingLogin",
  components: { JoinDatalandButton, UserAuthenticationButtons, Card },
});
</script>
<style scoped>
.d-text-register {
  color: #5a4f36;
}
</style>
