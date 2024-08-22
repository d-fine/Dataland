<template>
  <div class="d-header m-0 fixed top-0 surface-900 h-4rem w-full grid align-items-center">
    <div class="col-4 text-left">
      <router-link to="/">
        <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland" class="pl-3" />
      </router-link>
    </div>

    <div class="col-4">
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
import { inject, defineComponent } from 'vue';
import AuthSection from '@/components/resources/newLandingPage/AuthSection.vue';
export default defineComponent({
  name: 'TheHeader',
  components: { AuthSection, UserProfileDropDown },
  props: {
    showUserProfileDropdown: {
      type: Boolean,
      default: true,
    },
  },
  setup() {
    return {
      authenticated: inject<boolean>('authenticated'),
    };
  },
});
</script>
