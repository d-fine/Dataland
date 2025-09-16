<template>
  <div class="dataland-header m-0 fixed top-0 h-4rem w-full grid align-items-center">
    <div class="col-4 text-left">
      <router-link to="/">
        <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland" />
      </router-link>
    </div>

    <div class="col-4">
      <slot />
    </div>

    <div class="col-4 flex justify-content-end" v-if="authenticated">
      <UserProfileDropDown />
    </div>
    <div class="col-4 flex justify-content-end" v-else>
      <AuthSection :is-landing-page="false" />
    </div>
  </div>
  <!-- This is a spacer div whose only purpose is to ensure that no elements get hidden behind the header -->
  <div class="h-4rem" />
  <DatasetsTabMenu />
</template>

<script lang="ts">
import UserProfileDropDown from '@/components/general/UserProfileDropDown.vue';
import AuthSection from '@/components/resources/landingPage/AuthSection.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'TheHeader',
  components: { AuthSection, UserProfileDropDown, DatasetsTabMenu },
  setup() {
    return {
      authenticated: inject<boolean>('authenticated'),
    };
  },
});
</script>

<style scoped>
.dataland-header {
  background-color: var(--p-highlight-background);
  font-size: var(--font-size-sm);
  z-index: 100;

  img {
    padding-left: var(--spacing-md);
    height: 1.5rem;
  }
}
</style>
