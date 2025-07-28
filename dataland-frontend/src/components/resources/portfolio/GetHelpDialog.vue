<template>
  <div>
    <div class="flex items-center gap-4 mb-8">
      <label for="topic" class="formkit-label">Choose a topic</label>
      <InputText id="topic" class="flex-auto" autocomplete="off" />
    </div>
    <div class="flex items-center gap-4 mb-8">
      <label for="message" class="formkit-label">Your message to us</label>
      <InputText id="message" class="flex-auto" autocomplete="off" />
    </div>
  </div>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';
</script>
