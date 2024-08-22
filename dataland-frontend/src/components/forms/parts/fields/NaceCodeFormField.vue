<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <NaceCodeSelector v-model="selectedNaceCodes" :shouldDisableCheckboxes="shouldDisableCheckboxes" />
    <!--
    Note: It is required to set the id of this div to the FormKit node Id to allow the checkCustomInputs methods
    in the validationUtils.ts file to scroll to this component when an error is detected. This is because the FormKit
    List type does not create a wrapper component on its own.
    -->
    <div :id="formKitNaceCodeInput?.node?.props?.id || undefined">
      <FormKit
        type="list"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :name="name"
        ref="formKitNaceCodeInput"
        v-model="selectedNaceCodes"
        outer-class="hidden-input"
      >
        <FormKitMessages />
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent, ref } from 'vue';
import { FormKit, FormKitMessages } from '@formkit/vue';
import NaceCodeSelector from '@/components/forms/parts/elements/derived/NaceCodeSelector.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'NaceCodeFormField',
  components: { NaceCodeSelector, FormKit, FormKitMessages, UploadFormHeader },
  setup() {
    return {
      formKitNaceCodeInput: ref(),
    };
  },
  data() {
    return {
      innerSelectedNaceCodes: [] as Array<string>,
    };
  },
  computed: {
    selectedNaceCodes: {
      get(): [] {
        return this.selectedNaceCodesBind ?? this.innerSelectedNaceCodes;
      },
      set(newValue: []) {
        this.$emit('update:selectedNaceCodesBind', newValue);
        this.innerSelectedNaceCodes = newValue;
      },
    },
  },

  props: {
    ...BaseFormFieldProps,
    selectedNaceCodesBind: {
      type: Array,
    },
    shouldDisableCheckboxes: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
});
</script>
