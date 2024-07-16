<template>
  <div class="form-field" :data-test="dataTest">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElement
      ref="multiSelectFormElementRef"
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      inputClass="long"
      :emptyMessage="emptyMessage"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';

import MultiSelectFormElement from '@/components/forms/parts/elements/basic/MultiSelectFormElement.vue';
import { DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'MultiSelectFormField',
  components: { MultiSelectFormElement, UploadFormHeader },
  inheritAttrs: false,
  props: {
    ...DropdownOptionFormFieldProps,
    dataTest: {
      type: String,
      default: 'multiSelectFormElement',
    },
    emptyMessage: {
      type: String,
    },
  },
});
</script>
