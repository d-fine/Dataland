<template>
  <transition name="p-message" appear>
    <div v-show="visible" :class="containerClass" class="p-message p-component border-round-sm">
      <div class="p-message-wrapper">
        <div data-test="left-icon" class="text-left left-icon">
          <slot name="left-icon"></slot>
        </div>
        <div data-test="text-info" class="text-left">
          <slot name="text-info"></slot>
        </div>
        <div data-test="action-button" class="text-right action-button ml-auto">
          <slot name="action-button"></slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'MessageComponent',
  props: {
    severity: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      visible: true,
    };
  },
  computed: {
    containerClass() {
      return ' p-message-' + this.severity;
    },
  },
});
</script>

<style scoped>
.action-button button {
  white-space: nowrap;
  margin-left: 15px;
}
.p-message-wrapper {
  display: flex;
  align-items: center;
}

.left-icon {
  place-self: flex-start;
  margin: 0.5rem 0;
}

.p-message {
  .p-message-wrapper {
    padding: 1rem 1.5rem;
  }
  .p-message-close {
    width: 2rem;
    height: 2rem;
    border-radius: 50%;
    background: transparent;
    transition:
      background-color 0.2s,
      color 0.2s,
      box-shadow 0.2s;
  }
  margin: 1rem 0;
  border-radius: 4px;
  border-width: 0 0 0 4px;
  &.p-message-light-error {
    background: var(--default-neutral-white);
    border: 1px solid var(--message-error-border);
  }
  &.p-message-light-success {
    background: var(--default-neutral-white);
    border: 1px solid var(--message-success-border);
  }
  &.p-message-block {
    background: var(--message-block-bg);
    border: solid var(--message-block-border);
    color: var(--message-block-text);
  }
  &.p-message-info {
    background: var(--message-info-bg);
    border: solid var(--message-info-border);
    color: var(--message-info-text);
  }
  &.p-message-bordered-success {
    background: var(--message-bordered-success-bg);
    border: solid var(--message-bordered-success-border);
    color: var(--message-bordered-success-text);
  }
  &.p-message-success {
    background: var(--message-success-bg);
    border: solid var(--message-success-border);
    color: var(--message-success-text);
  }
  &.p-message-warn {
    background: var(--message-warn-bg);
    border: solid var(--message-warn-border);
    color: var(--message-warn-text);
  }
  &.p-message-error {
    background: var(--message-error-bg);
    border: solid var(--message-error-border);
    color: var(--message-error-text);
  }
  .p-message-text {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-medium);
    overflow-wrap: break-word;
    max-width: 100%;
  }
  .p-message-icon {
    font-size: var(--font-size-xl);
    margin-right: 1rem;
  }
  h4 {
    margin: 0.5rem 0.25rem;
  }
}
</style>
