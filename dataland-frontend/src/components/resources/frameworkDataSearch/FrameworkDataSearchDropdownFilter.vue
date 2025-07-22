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
