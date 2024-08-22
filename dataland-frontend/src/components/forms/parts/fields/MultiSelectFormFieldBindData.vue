<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElementBindData
      :name="name"
      v-model:selectedItemsBind="internalSelections"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      :inner-class="inputClass"
      :optionLabel="optionLabel"
      :optionValue="optionValue"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import MultiSelectFormElementBindData from '@/components/forms/parts/elements/basic/MultiSelectFormElementBindData.vue';
import { MultiSelectFormProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'MultiSelectFormFieldBindData',
  components: { MultiSelectFormElementBindData, UploadFormHeader },
  emits: ['update:selectedItemsBindInternal'],
  computed: {
    internalSelections: {
      get(): Array<string> {
        return this.selectedItemsBindInternal;
      },
      set(newValue: Array<string>) {
        this.$emit('update:selectedItemsBindInternal', newValue);
      },
    },
  },
  props: {
    ...MultiSelectFormProps,
    selectedItemsBindInternal: {
      type: Array as () => Array<string>,
      default: () => [],
    },
  },
});
</script>
