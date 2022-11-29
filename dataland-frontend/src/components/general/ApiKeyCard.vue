<template>
  <div class="col-12">
    <div class="surface-card shadow-2 p-3 border-round">
      <div class="flex justify-content-between mb-3">
        <div>
          <div class="text-900 font-medium text-xl text-left">API Key info</div>
          <span class="block text-500 mb-3">The API Key will expire on {{ expiryDateCalculation }}</span>
        </div>

          <div class="">Scope
            <div class="flex align-items-center justify-content-center ">
          <div class="bg-yellow-100 border-round">
            <span class="text-yellow-600 text-xl">READ</span>
          </div>
          <div class="bg-green-100 border-round">
            <span class="text-green-500 text-xl">WRITE</span>
          </div>
          </div>
        </div>
      </div>
      <Button label="MODIFY" icon="pi pi-pencil" class="p-button-text" />
      <Button label="DELETE" icon="pi pi-trash" class="p-button-outlined p-button-danger" />
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
