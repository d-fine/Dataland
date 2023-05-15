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
    <UploadCertificatesForm v-show="certificateRequiredIfYes && yesSelected" />
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
  //watch: documentrequiredIfYes(){
  //  if yes && certificateRequiredIfYes == true {
  //    documentRequired = true;
  //  }
  //}
  data() {
    return {
      yesSelected: false,
    };
  },
  methods: {
    /**
     * Sets the value yesSelected to true when "Yes" is selected
     * @param event the "Yes" / "No" selection event
     */
    setCertificateRequired(event: Event) {
      console.log(event);
      this.yesSelected = (event as InputEvent).data === "Yes";
    },
  },
});
</script>
