<template>
  <span
    class="toggle-chip mr-2 mb-2"
    :class="`${isToggled ? 'toggled' : ''}`"
    data-test="toggle-chip"
    @click="toggle()"
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
  },
  data() {
    return {
      isToggled: false,
    };
  },
  methods: {
    /**
     * Toggles chip on and off
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
