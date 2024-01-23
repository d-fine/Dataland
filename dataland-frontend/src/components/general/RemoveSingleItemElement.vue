<template>
  <div class="remove-single-item px-2" data-test="removeSingleItemElement">
    <span class="label">{{ label }}</span>
    <PrimeButton type="button" label="REMOVE" name="remove" text />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";

export default defineComponent({
  components: {
    PrimeButton,
  },
  name: "RemoveSingleItemElement",
  props: {
    label: {
      type: String,
      required: true,
    },
  },
});
</script>

<style scoped lang="scss">
.remove-single-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--gray-100);
  margin-bottom: 1.5rem;

  .label {
    font-family: monospace;
  }
}
</style>
