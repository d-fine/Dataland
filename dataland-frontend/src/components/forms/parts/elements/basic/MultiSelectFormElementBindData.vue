<template>
  <MultiSelect
    v-model="selections"
    :options="options"
    :placeholder="placeholder"
    :show-toggle-all="false"
    :class="inputClass"
    :optionValue="optionValue"
    :optionLabel="optionLabel"
    :maxSelectedLabels="3"
  />
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import MultiSelect from 'primevue/multiselect';
import { MultiSelectFormProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'MultiSelectFormElementBindData',
  components: { MultiSelect },
  emits: ['update:selectedItemsBind'],
  computed: {
    selections: {
      get(): Array<string> {
        return this.selectedItemsBind;
      },
      set(newValue: Array<string>) {
        this.$emit('update:selectedItemsBind', newValue);
      },
    },
  },
  props: {
    ...MultiSelectFormProps,
    selectedItemsBind: {
      type: Array as PropType<Array<string>>,
      default: () => [],
    },
  },
});
</script>
