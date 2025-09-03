<template>
  <div class="dataland-header m-0 fixed top-0 h-4rem w-full grid align-items-center">
    <div class="col-4 text-left">
      <router-link to="/">
        <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland" />
      </router-link>
    </div>

    <div class="col-4">
      <DatasetsTabMenu :initialTabIndex="initialTabIndex" />
      <slot />
    </div>

    <div class="col-4 flex justify-content-end" v-if="showUserProfileDropdown && authenticated">
      <UserProfileDropDown />
    </div>
    <div class="col-4 flex justify-content-end" v-if="!authenticated">
      <AuthSection :is-landing-page="false" />
    </div>
  </div>
  <!-- This is a spacer div whose only purpose is to ensure that no elements get hidden behind the header -->
  <div class="h-4rem" />
</template>

<script lang="ts">
import UserProfileDropDown from '@/components/general/UserProfileDropDown.vue';
import AuthSection from '@/components/resources/landingPage/AuthSection.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { defineComponent, inject, computed } from 'vue';
import { useRoute } from 'vue-router';

export default defineComponent({
  name: 'TheHeader',
  components: { AuthSection, UserProfileDropDown, DatasetsTabMenu },
  props: {
    showUserProfileDropdown: {
      // todo: Do we need this?
      type: Boolean,
      default: true,
    },
  },
  setup() {
    const route = useRoute();
    const initialTabIndex = computed(() => route.meta.initialTabIndex ?? 0);
    return {
      authenticated: inject<boolean>('authenticated'),
      initialTabIndex,
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
