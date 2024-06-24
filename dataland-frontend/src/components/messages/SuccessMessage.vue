<template>
  <div>
    <Message severity="success" :sticky="true" :key="messageId" :closable="closable">
      <h4>{{ successMessage }}</h4>
      <p v-if="message">{{ message }}</p>
    </Message>
  </div>
</template>

<script lang="ts">
import Message from 'primevue/message';

export default {
  name: 'SuccessMessage',
  components: { Message },
  props: {
    successMessage: {
      type: String,
      default: 'Upload successfully executed.',
    },
    message: {
      type: String,
    },
    messageId: Number,
    closable: { type: Boolean, default: true },
  },
};
</script>
