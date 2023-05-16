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
      @input="setCertificateRequired($event)"
    />
    <UploadCertificatesForm
      v-show="certificateRequiredIfYes && yesSelected"
      @certificatesChanged="emitCertificateUpdatedEvent"
      ref="uploadCertificatesForm"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadCertificatesForm from "@/components/forms/parts/elements/basic/UploadCertificatesForm.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadCertificatesForm },
  inheritAttrs: false,
  props: YesNoFormFieldProps,
  data() {
    return {
      yesSelected: false,
    };
  },
  emits: ["certificateUpdated"],
  watch: {
    yesSelected() {
      this.deleteCertificate();
    },
  },
  methods: {
    /**
     * Sets the value yesSelected to true when "Yes" is selected
     * @param event the "Yes" / "No" selection event
     */
    setCertificateRequired(event: Event) {
      this.yesSelected = (event as unknown as string) === "Yes";
    },

    /**
     * If "No" is reselected after one has uploaded a file, this handles removing the file(s) and clearing
     * the certificate list.
     */
    deleteCertificate() {
      if (!this.yesSelected) {
        const fileNumber = this.$refs.uploadCertificatesForm.$refs.fileUpload.files.length as number;
        if (fileNumber > 0) {
          this.$refs.uploadCertificatesForm.$refs.fileUpload.files = [];
          this.$refs.uploadCertificatesForm.removeAllCertificates();
        }
      }
    },

    /**
     * Emits event that selected certificate changed
     */
    emitCertificateUpdatedEvent() {
      this.$emit("certificateUpdated", this.name, this.$refs.uploadCertificatesForm.certificatesToUpload[0]);
    },

    updateCertificateReferenceOnDataPoint() {
      //TODO: actually implement this
      console.log(this.$refs.uploadCertificatesForm.certificatesToUpload[0].reference);
    },
  },
});
</script>
