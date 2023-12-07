<template>
  <div class="mb-3 p-0 -ml-2" :class="dataPointIsAvailable ? 'bordered-box' : ''">
    <div class="px-2 py-3 next-to-each-other vertical-middle" v-if="isDataPointToggleable">
      <RadioButtonsFormElement @update:currentValue="emitUpdateCurrentValue" :options="HumanizedYesNo" />
      <p>------->{{ dataPointIsAvailable }}</p>
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
    </div>

    <div class="p-2" v-if="showDataPointFields">
      <FormKit v-model="baseDataPoint" type="group" :name="name">
        <div class="col-12">
          <slot />
          <UploadDocumentsForm
            v-show="baseDataPoint.value === 'Yes'"
            @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
            ref="uploadDocumentsForm"
            :name="name"
            :more-than-one-document-allowed="false"
            :file-names-for-prefill="fileNamesForPrefill"
          />
          <FormKit v-if="baseDataPoint.value === 'Yes'" type="group" name="dataSource">
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

export default defineComponent({
  name: "BaseDataPointFormField",
  components: { UploadFormHeader, UploadDocumentsForm, InputSwitch, RadioButtonsFormElement },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,

    isDataPointToggleable: {
      type: Boolean,
      default: true,
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
  },
  methods: {
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
     * Toggle dataPointIsAvailable variable value
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
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
     * Emits an event when the currentValue has been changed
     * @param currentValue current value
     */
    emitUpdateCurrentValue(currentValue: string) {
      if (currentValue && currentValue !== "") {
        console.log("!!!!!!!!!!currentValue", currentValue);
        this.dataPointIsAvailable = true;
      } else {
        console.log("---------currentValue", currentValue);
        this.dataPointIsAvailable = false;
      }
    },
  },
});
</script>
