<template>
  <PrimeDialog id="revokeModal" :modal="true" header="Header" footer="Footer" v-model:visible="viewDeleteConfirmation">
    <template #header>
      <h2 class="m-0">Delete API Key</h2>
    </template>
    Are you sure you want to delete this API key?
    <strong>If you confirm, your previous token will be invalidated and your applications will stop working.</strong>
    <template #footer>
      <PrimeButton label="CANCEL" @click="deleteConfirmToggle" class="p-button-outlined" />
      <PrimeButton id="confirmRevokeButton" label="CONFIRM" @click="$emit('revokeKey')" />
    </template>
  </PrimeDialog>

  <div>
    <div id="existingApiKeyCard" class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left">API Key info</div>
          <span :class="{ 'text-red-700': !isKeyExpired() }" class="block text-600 mb-3 mt-6">
            {{ whenKeyExpire }}
          </span>
        </div>

        <div data-test="userRoles" class="pr-1">
          <UserRolesBadges :userRoles="userRoles" />
        </div>
      </div>
      <div class="col-12 text-right">
        <PrimeButton
          label="DELETE"
          icon="pi pi-trash"
          class="p-button-outlined ml-3 px-4 border-2 p-button-danger"
          @click="deleteConfirmToggle"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import PrimeDialog from "primevue/dialog";
import { formatExpiryDate, calculateDaysFromNow } from "@/utils/DateFormatUtils";
import { defineComponent } from "vue";
import UserRolesBadges from "@/components/general/apiKey/UserRolesBadges.vue";

export default defineComponent({
  setup() {
    return { formatExpiryDate, calculateDaysFromNow };
  },
  name: "ApiKeyCard",
  components: { PrimeButton, PrimeDialog, UserRolesBadges },
  props: {
    expiryDate: {
      type: Number,
      default: null,
    },
    userRoles: {
      type: Array,
    },
  },
  data: () => ({
    viewDeleteConfirmation: false,
  }),
  computed: {
    expiryDateInDays() {
      return calculateDaysFromNow(this.expiryDate);
    },

    whenKeyExpire() {
      if (this.expiryDate && this.expiryDate >= Date.now()) {
        return `The API Key will expire on ${formatExpiryDate(this.expiryDateInDays)}`;
      } else if (this.expiryDate && this.expiryDate < Date.now()) {
        return `The API Key expired ${formatExpiryDate(this.expiryDateInDays)}`;
      } else {
        return "The API Key has no defined expire date";
      }
    },
  },
  methods: {
    deleteConfirmToggle() {
      this.viewDeleteConfirmation = !this.viewDeleteConfirmation;
    },
    isKeyExpired() {
      return this.expiryDate >= new Date().getTime();
    },
  },
});
</script>
