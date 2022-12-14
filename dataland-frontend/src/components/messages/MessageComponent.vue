<template>
  <transition name="p-message" appear>
    <div v-show="visible" :class="containerClass" class="p-message p-component border-round-sm">
      <div class="p-message-wrapper">
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
import { defineComponent } from "vue";

export default defineComponent({
  name: "MessageComponent",
  props: {
    severity: {
      type: String,
      default: null,
    },
    closable: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      visible: true,
    };
  },
  computed: {
    containerClass() {
      return " p-message-" + this.severity;
    },
  },
});
</script>

<style>
.action-button button {
  white-space: nowrap;
  margin-left: 15px;
}
.p-message-wrapper {
  display: flex;
  align-items: center;
}
</style>
