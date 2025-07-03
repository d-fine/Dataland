<template>
  <div class="text-left text-xs ml-1 text-600">Scope</div>
  <div class="flex align-items-center justify-content-center">
    <Tag v-if="userRoles.includes(KEYCLOAK_ROLE_USER)" data-test="userRoleUser" value="Read" severity="warn"></Tag>
    <Tag
      v-if="userRoles.includes(KEYCLOAK_ROLE_UPLOADER)"
      data-test="userRoleUploader"
      value="WRITE"
      severity="success"
    ></Tag>
    <Tag
      v-if="userRoles.includes(KEYCLOAK_ROLE_REVIEWER)"
      data-test="userRoleReviewer"
      value="REVIEWER"
      severity="danger"
    ></Tag>
    <Tag v-if="userRoles.includes(KEYCLOAK_ROLE_ADMIN)" data-test="userRoleAdmin" value="ADMIN" severity="info"></Tag>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import Tag from 'primevue/tag';

import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_REVIEWER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
} from '@/utils/KeycloakRoles';

defineProps<{
  userRoles: string[];
}>();
</script>
