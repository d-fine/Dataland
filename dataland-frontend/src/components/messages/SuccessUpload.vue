<template>
  <div>
    <Message severity="success" :sticky="false" :life="5000" :closable="false" :key:="messageId">
      <h4>{{ message }}</h4>
    </Message>
  </div>
</template>

<script lang="ts">
import Message from "primevue/message";

export default {
  name: "SuccessUpload",
  components: { Message },
  props: {
    message: String,
    messageId: Number,
  },
};
</script>
