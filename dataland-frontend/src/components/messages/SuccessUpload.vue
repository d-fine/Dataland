<template>
  <div>
    <Message severity="success" :sticky="true" :key:="messageId">
      <h4>Upload successfully executed.</h4>
      <h4 v-if="message">{{ message }}</h4>
    </Message>
  </div>
</template>

<script lang="ts">
import Message from "primevue/message";

export default {
  name: "SuccessUpload",
  components: { Message },
  props: {
    message: {
      type: String,
      default: "",
    },
    messageId: Number,
  },
};
</script>
