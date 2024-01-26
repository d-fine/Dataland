<template>
  <div class="remove-single-item px-2" data-test="removeAddSingleItemElement">
    <span class="label">{{ label }}</span>
    <PrimeButton v-if="!removed" type="button" @click="onClick('removed')" label="REMOVE" text />
    <PrimeButton v-if="removed" type="button" @click="onClick('undo')" label="UNDO" text />
  </div>
  <slot v-if="!removed" name="default" />
  <slot v-if="removed" name="removed" />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";

export default defineComponent({
  components: {
    PrimeButton,
  },
  emits: ["removed", "undo"],
  name: "RemoveAddSingleItemElement",
  data() {
    return {
      removed: false,
    };
  },
  props: {
    label: {
      type: String,
      required: true,
    },
    value: {
      type: [String, Number, Array, Object],
      required: true,
    },
  },
  methods: {
    /**
     * Emit item's value and disable button
     * @param eventName whether we're removing or reverting the value
     */
    onClick(eventName: "removed" | "undo") {
      this.$emit(eventName, this.value);
      this.removed = eventName === "removed";
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

  .label {
    font-family: monospace;
  }
}
</style>
