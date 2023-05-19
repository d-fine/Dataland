<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <FormKit v-if="certificateRequiredIfYes" type="group" :name="name">
      <RadioButtonsFormElement
        name="value"
        :validation="validation"
        :validation-label="validationLabel ?? displayName"
        :options="[
          {
            label: 'Yes',
            value: 'Yes',
          },
          {
            label: 'No',
            value: 'No',
          },
        ]"
        @input="setDocumentRequired($event)"
      />
      <UploadDocumentsForm
        v-show="yesSelected"
        @documentsChanged="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        :more-than-one-document-allowed="false"
      />
      <FormKit v-if="yesSelected" type="group" name="dataSource" validation="required|notEmpty">
        <FormKit type="hidden" name="name" :modelValue="documentName" />
        <FormKit type="hidden" name="reference" :modelValue="documentReference" />
      </FormKit>
    </FormKit>

    <RadioButtonsFormElement
      v-else
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? displayName"
      :options="[
        {
          label: 'Yes',
          value: 'Yes',
        },
        {
          label: 'No',
          value: 'No',
        },
      ]"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { DocumentToUpload } from "@/utils/FileUploadUtils";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: YesNoFormFieldProps,
  data() {
    return {
      yesSelected: false,
      documentName: "",
      documentReference: "",
    };
  },

  emits: ["documentUpdated"],
  watch: {
    yesSelected() {
      this.deleteDocument();
    },
  },
  methods: {
    /**
     * Sets the value yesSelected to true when "Yes" is selected
     * @param event the "Yes" / "No" selection event
     */
    setDocumentRequired(event: Event) {
      this.yesSelected = (event as unknown as string) === "Yes";
    },

    /**
     * If "No" is reselected after one has uploaded a file, this handles removing the file(s) and clearing
     * the certificate list.
     */
    deleteDocument() {
      if (!this.yesSelected) {
        console.log("Now remove all documents");
        this.$refs.uploadDocumentsForm.removeAllDocuments();
      }
    },

    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.documentName = updatedDocuments[0]?.fileNameWithoutSuffix ?? "";
      this.documentReference = updatedDocuments[0]?.reference ?? "";
      console.log(this.documentName, this.documentReference);
      this.$emit("documentUpdated", this.name, updatedDocuments[0]);
    },

    notEmpty(): boolean {
      return this.documentName !== "";
    },
  },
});
</script>
