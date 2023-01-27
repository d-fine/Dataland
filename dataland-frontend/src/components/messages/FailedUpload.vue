<template>
  <div>
    <Message severity="error" :sticky="true" :key:="messageId">
      <h4>Sorry, but an Error occurred.</h4>
      <h4>{{ message }}</h4>
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
