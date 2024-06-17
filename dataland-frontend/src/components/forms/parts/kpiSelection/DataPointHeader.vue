<template>
  <div class="form-field-label">
    <h3>{{ name }}</h3>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "DataPointHeader",
  props: {
    name: String,
  },
});
</script>
