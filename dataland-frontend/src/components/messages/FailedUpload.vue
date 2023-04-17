<template>
  <div>
    <Message severity="error" :sticky="true" :key="messageId">
      <h4>Sorry, but an Error occurred.</h4>
      <p>{{ message }}</p>
    </Message>
  </div>
</template>

<script lang="ts">
import Message from "primevue/message";

export default {
  name: "FailedUpload",
  components: { Message },
  props: {
    message: String,
    messageId: Number,
  },
};
</script>

<style scoped>
::v-deep(.p-message-wrapper){
  padding: .25rem;
}
</style>
