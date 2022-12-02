<template>
  <div>
    <div class="surface-card shadow-1 p-3 border-round-sm border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left">API Key info</div>
          <span class="block text-600 mb-3 mt-6">The API Key will expire on {{ expiryDateCalculation }}</span>
        </div>

          <div>
            <div class="text-left text-xs ml-1 text-600">Scope</div>
            <div class="flex align-items-center justify-content-center ">
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
        <Button label="MODIFY" icon="pi pi-pencil" class="p-button-text ml-3 px-4 text-sm font-bold" />
        <Button label="DELETE" icon="pi pi-trash" class="p-button-outlined ml-3 px-4 text-sm border-2 p-button-danger font-bold" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import Button from "primevue/button";
import {defineComponent} from "vue";

export default defineComponent({
  name: "ApiKeyCard",
  components: { Button, },
  props: {},
  computed: {
    expiryDateCalculation() {
      return new Date().toLocaleDateString()
    },
  },
});
</script>

<style>
</style>
