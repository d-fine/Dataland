<template>
  <div class="mb-3" :data-test="name">
    <div class="">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <FormKit
        type="checkbox"
        name="name"
        v-model="checkboxValue"
        :options="HumanizedYesNo"
        :outer-class="{
          'yes-no-radio': true,
        }"
        :inner-class="{
          'formkit-inner': false,
        }"
        :input-class="{
          'formkit-input': false,
          'p-radiobutton': true,
        }"
        :ignore="true"
        :plugins="[disabledOnMoreThanOne]"
        @input="updateCurrentValue($event)"
      />
    </div>

    <div v-if="showDataPointFields">
      <FormKit v-model="dataPoint" type="group" :name="name">
        <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :validation="validation"
          :validation-label="validationLabel"
          :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
        />
        <div v-if="dataPoint.value === 'Yes'">
          <FormListFormField
            :name="name"
            :label="label"
            :required="required"
            :validation="validation"
            :validation-label="validationLabel"
            sub-form-component="UploadDocumentsFormWithComment"
            data-test-add-button="addNewProductButton"
            label-add-button="ADD NEW Product"
            data-test-sub-form="productSection"
          />
        </div>
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import type { DocumentToUpload } from "@/utils/FileUploadUtils";

import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { disabledOnMoreThanOne } from "@/utils/FormKitPlugins";
import { HumanizedYesNo } from "@/utils/YesNoNa";
import { type ListOfBaseDataPointsFormField } from "@/utils/DataPoint";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";

export default defineComponent({
  name: "ListOfBaseDataPointsFormField",
  components: { FormListFormField, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
  },
  emits: ["reportsUpdated"],
  data() {
    return {
      dataPoint: {} as ListOfBaseDataPointsFormField<unknown>,
      dataPointIsAvailable: false,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
      currentValue: null,
      checkboxValue: [] as Array<string>,
    };
  },
  computed: {
    HumanizedYesNo() {
      return HumanizedYesNo;
    },
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable;
    },
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix ?? "";
      this.documentReference = this.referencedDocument?.fileReference ?? "";
      this.$emit("fieldSpecificDocumentsUpdated", updatedDocuments[0]);
    },

    /**
     * updateCurrentValue
     * @param checkboxValue checkboxValue
     */
    updateCurrentValue(checkboxValue: [string]) {
      if (checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.dataPoint.value = checkboxValue[0].toString();
      } else {
        this.dataPointIsAvailable = false;
      }
    },
  },
});
</script>