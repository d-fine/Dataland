<template>
  <div class="text-left text-xs ml-1 text-600">Scope</div>
  <div class="flex align-items-center justify-content-center">
    <div v-if="userRoles.includes('ROLE_USER')" data-test="userRoleUser" class="p-badge badge-yellow">
      <span>READ</span>
    </div>
    <div v-if="userRoles.includes('ROLE_UPLOADER')" data-test="userRoleUploader" class="p-badge badge-green">
      <span>WRITE</span>
    </div>
    <div v-if="userRoles.includes('ROLE_ADMIN')" data-test="userRoleAdmin" class="p-badge badge-blue">
      <span>ADMIN</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "UserRolesBadges",
  props: {
    userRoles: {
      type: Array,
    },
  },
});
</script>
