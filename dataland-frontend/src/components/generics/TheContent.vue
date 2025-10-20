<template>
  <div class="d-content col-12 p-0">
    <slot />
  </div>
</template>

<script lang="ts">
export default {
  name: 'TheContent',
};
</script>
<style scoped>
.d-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
