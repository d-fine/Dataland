<template>
  <button :class="`${buttonType} button-font-style`" :aria-label="ariaLabel" @click="handleClick">
    {{ label }}
  </button>
</template>

<script setup lang="ts">
const { label, buttonType, ariaLabel } = defineProps({
  label: String,
  buttonType: {
    type: String,
    default: 'button-component',
  },
  ariaLabel: String,
});

const emit = defineEmits<(event: 'click') => void>();

const handleClick = (): void => {
  emit('click');
};
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';

.button-font-style {
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: 0.75px;
  text-transform: uppercase;
}

.button-component {
  padding: 14px 32px 12px;
  border-radius: 32px;
  background-color: var(--primary-orange);
  color: var(--default-neutral-white);
  border: 2px solid var(--primary-orange);
  cursor: pointer;
  @media only screen and (max-width: newVariables.$small) {
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
