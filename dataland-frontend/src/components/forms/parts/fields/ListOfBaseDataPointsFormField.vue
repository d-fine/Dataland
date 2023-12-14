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
      <FormKit v-model="dataPoint" type="list" :name="name">
        <div>
          <FormListFormField
            :name="name"
            :label="label"
            :required="required"
            :validation="validation"
            :validation-label="validationLabel"
            sub-form-component="UploadDocumentsFormWithComment"
            data-test-add-button="addNewProductButton"
            label-add-button="Neuen Parport hinzufügen"
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
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";
import { type BaseDataPointString } from "@clients/backend";
import InputSwitch from "primevue/inputswitch";

export default defineComponent({
  name: "ListOfBaseDataPointsFormField",
  components: { FormListFormField, UploadFormHeader, InputSwitch },
  // inject: {
  //   injectReportsNameAndReferences: {
  //     from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
  //     default: {} as ObjectType,
  //   },
  //   injectlistOfFilledKpis: {
  //     from: "listOfFilledKpis",
  //     default: [] as Array<string>,
  //   },
  // },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
  },
  emits: ["reportsUpdated"],
  data() {
    return {
      dataPoint: [] as BaseDataPointString[],
      // dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
      dataPointIsAvailable: false,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
    };
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable;
    },
  },
  methods: {
    /**
     * Toggle dataPointIsAvailable variable value
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
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
  },
});
</script>
