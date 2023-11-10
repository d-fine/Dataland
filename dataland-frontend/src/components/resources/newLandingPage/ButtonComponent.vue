<template>
  <button :class="buttonType" class="button-component-fontstyle" :aria-label="ariaLabel" @click="handleClick">
    {{ label }}
  </button>
</template>

<script setup lang="ts">
const { label, buttonType, ariaLabel } = defineProps<{
  label: string;
  buttonType: string;
  ariaLabel: string;
}>();

const emit = defineEmits<(event: "click") => void>();

const handleClick = (): void => {
  emit("click");
};
</script>

<style scoped lang="scss">
.button-component {
  &-fontstyle {
    font-size: 16px;
    font-weight: 600;
    line-height: 20px;
    letter-spacing: 0.75px;
  }
  &-landing {
    padding: 14px 32px;
    border-radius: 32px;
    background-color: var(--primary-orange);
    color: var(--default-neutral-white);
    border: 2px solid var(--primary-orange);
    text-transform: uppercase;
    cursor: pointer;

    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }

  &-login {
    padding: 10px 38px;
    border-radius: 0;
    color: var(--primary-orange);
    background-color: var(--basic-dark);
    border: 2px solid var(--primary-orange);
    text-transform: uppercase;
    cursor: pointer;

    &:hover {
      border: 2px solid var(--default-neutral-white);
      color: var(--default-neutral-white);
    }
  }

  &-registration {
    padding: 10px 38px;
    border-radius: 0;
    background-color: var(--primary-orange);
    color: var(--default-neutral-white);
    border: 2px solid var(--primary-orange);
    text-transform: uppercase;
    cursor: pointer;

    &:hover {
      color: var(--basic-dark);
      background-color: var(--default-neutral-white);
    }
  }
}
</style>
