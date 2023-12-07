<template>
  <div class="form-field">
    <div class="">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <FormKit
          type="checkbox"
          name="name"
          v-model="checkboxValue"
          :options="options"
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
      <FormKit v-model="baseDataPoint" type="group" :name="name">
        <FormKit
            type="text"
            name="value"
            v-model="currentValue"
            :validation="validation"
            :validation-label="validationLabel"
            :outer-class="{ 'hidden-input': true, 'formkit-outer': false, }"
        />
        <div class="col-12" v-if="baseDataPoint.value === 'Yes'">
          <UploadDocumentsForm
            @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
            ref="uploadDocumentsForm"
            :name="name"
            :more-than-one-document-allowed="false"
            :file-names-for-prefill="fileNamesForPrefill"
          />
          <FormKit type="group" name="dataSource">
            <FormKit type="hidden" name="fileName" v-model="documentName" />
            <FormKit type="hidden" name="fileReference" v-model="documentReference" />
          </FormKit>
        </div>
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import InputSwitch from "primevue/inputswitch";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import { type BaseDataPoint } from "@/utils/DataPoint";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { HumanizedYesNo } from "@/utils/YesNoNa";
import { disabledOnMoreThanOne } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "BaseDataPointFormField",
  components: { UploadFormHeader, UploadDocumentsForm, InputSwitch, RadioButtonsFormElement },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    options: {
      type: Object,
      require: true,
    },
  },
  inject: {
    injectlistOfFilledKpis: {
      from: "listOfFilledKpis",
      default: [] as Array<string>,
    },
  },
  data() {
    return {
      dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
      baseDataPoint: {} as BaseDataPoint<unknown>,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
      isMounted: false,

      key: 0,
      shouldBeIgnored: false,
      currentValue: "",
      checkboxValue: [],
    };
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable;
    },
    HumanizedYesNo() {
      return HumanizedYesNo;
    },
  },
  emits: ["reportsUpdated"],
  mounted() {

    this.updateFileUploadFiles();
    this.isMounted = true;
  },
  watch: {
    documentName() {
      if (this.isMounted) {
        this.updateFileUploadFiles();
      }
    },
    currentValue(newVal) {
      this.setCheckboxValue(newVal)
    },
  },
  methods: {
    disabledOnMoreThanOne,

    setCheckboxValue(newCheckboxValue) {
      console.log('newCheckboxValue', newCheckboxValue)
      if (this.currentValue && this.currentValue !== "") {
        this.checkboxValue = [this.currentValue];
      }
    },

    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix ?? "";
      this.documentReference = this.referencedDocument?.fileReference ?? "";
      this.$emit("reportsUpdated", this.documentName, this.referencedDocument);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      if (this.documentName !== "" && this.referencedDocument === undefined) {
        this.fileNamesForPrefill = [this.documentName];
      }
    },
    /**
     * updateCurrentValue
     * @param checkboxValue checkboxValue
     */
    updateCurrentValue(checkboxValue: [string]) {
      console.log("checkboxValue BaseDataPointFormField", checkboxValue[0]);
      if (checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.baseDataPoint.value = checkboxValue[0].toString();
        console.log("this.baseDataPoint.value +++", this.baseDataPoint.value);
      } else {
        this.dataPointIsAvailable = false;
      }
    },
  },
});
</script>
