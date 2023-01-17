<template>
  <div class="text-left text-xs ml-1 text-600">Scope</div>
  <div class="flex align-items-center justify-content-center">
    <div
      v-if="userRoles.includes('ROLE_USER')"
      data-test="userRoleUser"
      class="bg-yellow-100 border-round px-2 border-round-sm m-1"
    >
      <span class="text-yellow-700 text-sm font-semibold">READ</span>
    </div>
    <div
      v-if="userRoles.includes('ROLE_UPLOADER')"
      data-test="userRoleUploader"
      class="bg-green-100 border-round px-2 border-round-sm m-1"
    >
      <span class="text-green-700 text-sm font-semibold">WRITE</span>
    </div>
    <div
      v-if="userRoles.includes('ROLE_ADMIN')"
      data-test="userRoleAdmin"
      class="bg-blue-100 border-round px-2 border-round-sm m-1"
    >
      <span class="text-blue-700 text-sm font-semibold">ADMIN</span>
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
