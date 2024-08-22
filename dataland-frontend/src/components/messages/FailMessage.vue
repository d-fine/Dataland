<template>
  <div>
    <Message severity="error" :sticky="true" :key="messageId" :closable="closable">
      <h4>Sorry, but an Error occurred.</h4>
      <p>{{ message }}</p>
      <p v-if="summary">{{ summary }}</p>
    </Message>
  </div>
</template>

<script lang="ts">
import Message from 'primevue/message';

export default {
  name: 'FailMessage',
  components: { Message },
  props: {
    message: String,
    messageId: Number,
    closable: { type: Boolean, default: true },
    summary: { type: String, default: '' },
  },
};
</script>
