<template>
  <h1>{{ data }}</h1>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "AlignedDataTableModal",
  components: {},
  inheritAttrs: false,
  inject: {},
  props: {
    data: {
      type: String,
      default: "Test",
    },
  },

  data() {
    return {};
  },
});
</script>
