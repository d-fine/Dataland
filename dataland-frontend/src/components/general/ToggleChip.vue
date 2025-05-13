<template>
  <span
    :title="disabled ? 'No data available' : ''"
    :class="{
      toggled: isToggled,
      disabled: disabled,
    }"
    class="toggle-chip mr-2 mb-2"
    data-test="toggle-chip"
    @click="!disabled && toggle()"
  >
    <span class="label">
      {{ label }}
    </span>
  </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'ToggleChip',
  emits: ['onChange'],

  props: {
    label: {
      type: String,
      required: true,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      isToggled: false,
    };
  },

  methods: {
    /**
     * Toggles chip on and off and emits the change event
     * Does nothing if the chip is disabled
     */
    toggle(): void {
      this.isToggled = !this.isToggled;
      this.$emit('onChange', this.isToggled);
    },
  },
});
</script>

<style scoped lang="scss">
.toggle-chip {
  padding: 8px 12px;
  border-radius: 32px;
  border: 2px solid var(--text-color-third);
  gap: 4px;
  cursor: pointer;

  &:hover {
    border-color: var(--fk-color-primary);
    color: var(--fk-color-primary);
  }

  &.toggled {
    border-color: var(--fk-color-primary);
    background-color: var(--fk-color-primary);

    .label {
      color: white;
    }
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
    border-color: var(--text-color-third);

    &:hover {
      border-color: var(--text-color-third);
      color: inherit;
    }
  }

  .label {
    font-size: 16px;
    font-weight: 500;
    line-height: 21px;
    letter-spacing: 0.4px;
    text-align: left;
    user-select: none;
  }
}
</style>
