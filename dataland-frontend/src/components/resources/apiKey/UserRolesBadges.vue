<template>
  <div class="text-left text-xs ml-1 text-600">Scope</div>
  <div class="flex align-items-center justify-content-center">
    <div v-if="userRoles.includes(KEYCLOAK_ROLE_USER)" data-test="userRoleUser" class="p-badge badge-yellow">
      <span>READ</span>
    </div>
    <div v-if="userRoles.includes(KEYCLOAK_ROLE_UPLOADER)" data-test="userRoleUploader" class="p-badge badge-green">
      <span>WRITE</span>
    </div>
    <div v-if="userRoles.includes(KEYCLOAK_ROLE_REVIEWER)" data-test="userRoleReviewer" class="p-badge badge-purple">
      <span>REVIEWER</span>
    </div>
    <div v-if="userRoles.includes(KEYCLOAK_ROLE_ADMIN)" data-test="userRoleAdmin" class="p-badge badge-blue">
      <span>ADMIN</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_REVIEWER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
} from '@/utils/KeycloakRoles.ts';

export default defineComponent({
  name: 'UserRolesBadges',
  props: {
    userRoles: {
      type: Array as PropType<string[]>,
      required: true,
    },
  },
  data() {
    return {
      KEYCLOAK_ROLE_USER,
      KEYCLOAK_ROLE_UPLOADER,
      KEYCLOAK_ROLE_REVIEWER,
      KEYCLOAK_ROLE_ADMIN,
    };
  },
});
</script>
