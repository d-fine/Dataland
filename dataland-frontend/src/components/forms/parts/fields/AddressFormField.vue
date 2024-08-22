<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <AddressFormElement
      :name="name"
      :label="label"
      :validation="validation"
      :validation-label="validationLabel ?? label"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import AddressFormElement from '@/components/forms/parts/elements/derived/AddressFormElement.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'AddressFormField',
  components: { AddressFormElement, UploadFormHeader },
  props: BaseFormFieldProps,
});
</script>
