<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <RadioButtonsFormElement
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
      @input="setDocumentRequired($event)"
    />
    <UploadDocumentsForm
      v-show="certificateRequiredIfYes && yesSelected"
      @documentsChanged="emitDocumentUpdatedEvent"
      ref="uploadDocumentsForm"
      :more-than-one-document-allowed="false"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: YesNoFormFieldProps,
  data() {
    return {
      yesSelected: false,
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
        const fileNumber = this.$refs.uploadDocumentsForm.$refs.fileUpload.files.length as number;
        if (fileNumber > 0) {
          this.$refs.uploadDocumentsForm.$refs.fileUpload.files = [];
          this.$refs.uploadDocumentsForm.removeAllDocuments();
        }
      }
    },

    /**
     * Emits event that selected document changed
     */
    emitDocumentUpdatedEvent() {
      this.$emit("documentUpdated", this.name, this.$refs.uploadDocumentsForm.documentsToUpload[0]);
    },

    updateCertificateReferenceOnDataPoint() {
      //TODO: actually implement this
      console.log(this.$refs.uploadDocumentsForm.documentsToUpload[0].reference);
    },
  },
});
</script>
