<template>
  <dialog open>
    <slot></slot>
  </dialog>
</template>

<script lang="ts">
export default {
  name: "ClaimDatasetOwnerShipDialog"
}
</script>

<style lang="scss" scoped>
.dialog {
  margin: 0;
  position: fixed;
  top: 20vh;
  left: 30%;
  width: 40%;
  background-color: var(--surface-card);
}
</style>