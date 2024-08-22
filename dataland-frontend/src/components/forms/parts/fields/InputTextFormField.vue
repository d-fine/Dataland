<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit
      type="text"
      :name="name"
      :validation-label="validationLabel ?? label"
      :validation="validation"
      :placeholder="placeholder"
      outer-class="short"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'InputTextFormField',
  components: { FormKit, UploadFormHeader },
  props: FormFieldPropsWithPlaceholder,
});
</script>
