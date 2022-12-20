<template>
  <div class="middle-center-div">
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "MiddleCenterDiv",
});
</script>

<style scoped>
.middle-center-div {
  flex: 1;
  align-items: center;
  /* align-content: center; */
  display: flex;
  justify-content: center;
  justify-items: center;
}
</style>
