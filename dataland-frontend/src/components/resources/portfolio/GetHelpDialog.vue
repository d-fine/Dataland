<template>
  <Dialog v-model:visible="visible" modal header="Request of Support" :style="{ width: '25rem' }">
    <div class="flex items-center gap-4 mb-8">
      <label for="topic" class="formkit-label">Choose a topic</label>
      <InputText id="topic" class="flex-auto" autocomplete="off" />
    </div>
    <div class="flex items-center gap-4 mb-8">
      <label for="message" class="formkit-label">Your message to us</label>
      <InputText id="message" class="flex-auto" autocomplete="off" />
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import { ref } from 'vue';
const visible = ref(true);
</script>
