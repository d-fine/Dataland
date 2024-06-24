<template>
  <div class="mb-3" :data-test="name">
    <div class="px-2 py-3 next-to-each-other vertical-middle">
      <InputSwitch
        data-test="dataPointToggleButton"
        inputId="dataPointIsAvailableSwitch"
        @click="dataPointAvailableToggle"
        v-model="dataPointIsAvailable"
      />
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
    </div>

    <div v-if="showDataPointFields">
      <div>
        <FormListFormField
          :name="name"
          :label="label"
          :required="required"
          :validation="validation"
          :validation-label="validationLabel"
          sub-form-component="StringBaseDataPointFormField"
          data-test-add-button="addNewProductButton"
          label-add-button="Weiteres Dokument hinzügen"
          data-test-sub-form="productSection"
          @fieldSpecificDocumentsUpdated="fieldSpecificDocumentsUpdated"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import type { DocumentToUpload } from '@/utils/FileUploadUtils';

import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import InputSwitch from 'primevue/inputswitch';

export default defineComponent({
  name: 'ListOfBaseDataPointsFormField',
  components: { FormListFormField, UploadFormHeader, InputSwitch },
  inject: {
    injectlistOfFilledKpis: {
      from: 'listOfFilledKpis',
      default: [] as Array<string>,
    },
  },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
  },
  emits: ['reportsUpdated'],
  data() {
    return {
      dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
    };
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable;
    },
  },
  methods: {
    /**
     * Emits event that the selected document changed
     * @param referencedDocument the new referenced document
     */
    fieldSpecificDocumentsUpdated(referencedDocument: DocumentToUpload | undefined) {
      this.$emit('fieldSpecificDocumentsUpdated', referencedDocument);
    },
    /**
     * Toggle dataPointIsAvailable variable value
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
  },
});
</script>
