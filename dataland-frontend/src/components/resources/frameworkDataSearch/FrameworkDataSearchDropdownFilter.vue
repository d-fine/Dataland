<template>
  <MultiSelect
    v-model="selectedItemsBind"
    ref="multiselect"
    :options="availableItems"
    :filter="true"
    :showToggleAll="false"
    :filterPlaceholder="filterPlaceholder"
    optionLabel="displayName"
    optionDisabled="disabled"
    @before-show="overlayVisible = true"
    @before-hide="overlayVisible = false"
    :placeholder="filterName"
    :id="filterId"
  >
  </MultiSelect>
</template>

<script lang="ts">
import MultiSelect from 'primevue/multiselect';
import { defineComponent, ref } from 'vue';
import { type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';

export default defineComponent({
  setup() {
    return {
      multiselect: ref(),
    };
  },
  name: 'FrameworkDataSearchDropdownFilter',
  components: { MultiSelect },
  emits: ['update:modelValue'],
  props: {
    availableItems: {
      type: Array as () => Array<SelectableItem>,
      default: () => [],
    },
    modelValue: {
      type: Array as () => Array<SelectableItem>,
      default: () => [],
    },
    filterName: {
      type: String,
      default: '',
    },
    filterId: {
      type: String,
      default: '',
    },
    filterPlaceholder: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      overlayVisible: false,
    };
  },
  computed: {
    selectedItemsBind: {
      get(): Array<SelectableItem> {
        return this.modelValue;
      },
      set(newValue: Array<SelectableItem>) {
        this.$emit('update:modelValue', newValue);
      },
    },
  },
});
</script>

<style lang="scss" scoped>
:deep(.p-badge) {
  background: var(--default-neutral-white);
  color: #5a4f36;
}

:deep(.p-multiselect) {
  background: none;
  box-shadow: none;
}

:deep(.p-multiselect-trigger) {
  width: auto;
}

:deep(.selection-button) {
  background: white;
  color: #5a4f36;
  border: 2px solid #5a4f36;
  border-radius: 8px;
  height: 2.5rem;

  .selection-button-content {
    margin: 0.5rem 1rem;
  }

  &.overlayVisible {
    background: #e0dfde;
  }

  &.filterActive {
    background: #5a4f36;
    color: white;
  }
}
</style>
