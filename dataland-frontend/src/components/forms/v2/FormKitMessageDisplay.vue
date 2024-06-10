<template>
  <template v-if="Object.keys(props.messages).length > 0">
    <ul class="formkit-message-display">
      <template v-for="message in props.messages">
        <li v-if="message.visible">
          {{ message.value }}
        </li>
      </template>
    </ul>
  </template>
</template>

<script setup lang="ts">
const props = defineProps<{
  messages: Record<string, FormKitMessage>;
}>();
</script>
