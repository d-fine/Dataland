<template>
  <button class="button-component" :class="buttonType" :aria-label="ariaLabel" @click="handleClick">
    {{ label }}
  </button>
</template>

<script setup lang="ts">
import { defineProps } from "vue";

const { label, buttonType, ariaLabel } = defineProps<{
  label: string;
  buttonType: string;
  ariaLabel: string;
}>();

const emit = defineEmits<{
  (event: "click"): void;
}>();

const handleClick = (): void => {
  emit("click");
};
</script>

<style scoped lang="scss">
.button-component {
  padding: 14px 32px;
  border-radius: 32px;
  background-color: var(--primary-orange);
  color: var(--default-neutral-white);
  font-size: 16px;
  font-weight: 600;
  line-height: 20px;
  letter-spacing: 0.75px;
  text-transform: uppercase;
  border: 2px solid var(--primary-orange);
  cursor: pointer;
  @media only screen and (max-width: $small) {
    padding: 10px 16px;
    font-size: 14px;
    letter-spacing: 0.25px;
  }
  &:hover {
    background-color: var(--default-neutral-white);
    color: var(--basic-dark);
  }
}
</style>
