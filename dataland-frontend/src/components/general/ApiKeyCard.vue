<template>
  <PrimeDialog
    id="revokeModal"
    :modal="true"
    header="Header"
    footer="Footer"
    v-model:visible="viewDeleteConfirmation"
    :breakpoints="{ '960px': '75vw', '640px': '100vw' }"
    :style="{ width: '50vw' }"
  >
    <template #header>
      <h2 class="m-0">Delete API Key</h2>
    </template>
    Are you sure you want to delete this API key?
    <b>If you confirm, your previous token will be invalidated and your applications will stop working.</b>
    <template #footer>
      <PrimeButton label="CANCEL" @click="deleteConfirmToggle" class="p-button-outlined text-sm" />
      <PrimeButton id="confirmRevokeButton" label="CONFIRM" @click="$emit('revokeKey')" class="text-sm" />
    </template>
  </PrimeDialog>

  <div>
    <div id="existingApiKeyCard" class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left">API Key info</div>
          <span class="block text-600 mb-3 mt-6"
            >The API Key will expire on {{ formatExpiryDate(expiryDateInDays) }}</span
          >
        </div>

        <div>
          <div class="text-left text-xs ml-1 text-600">Scope</div>
          <div class="flex align-items-center justify-content-center">
            <div class="bg-yellow-100 border-round px-2 border-round-sm m-1">
              <span class="text-yellow-700 text-sm font-semibold">READ</span>
            </div>
            <div class="bg-green-100 border-round px-2 border-round-sm m-1">
              <span class="text-green-700 text-sm font-semibold">WRITE</span>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 text-right">
        <PrimeButton label="MODIFY" disabled icon="pi pi-pencil" class="p-button-text ml-3 px-4 text-sm font-bold" />
        <PrimeButton
          label="DELETE"
          icon="pi pi-trash"
          class="p-button-outlined ml-3 px-4 text-sm border-2 p-button-danger font-bold"
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

export default defineComponent({
  setup() {
    return { formatExpiryDate, calculateDaysFromNow };
  },
  name: "ApiKeyCard",
  components: { PrimeButton, PrimeDialog },
  props: {
    expiryDate: {
      type: Number,
      default: null,
    },
  },
  data: () => ({
    viewDeleteConfirmation: false,
  }),
  computed: {
    expiryDateInDays() {
      return calculateDaysFromNow(this.expiryDate);
    },
  },
  methods: {
    deleteConfirmToggle() {
      this.viewDeleteConfirmation = !this.viewDeleteConfirmation;
    },
  },
});
</script>

<style></style>
