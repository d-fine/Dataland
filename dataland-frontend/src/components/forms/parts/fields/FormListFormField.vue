<template>
  <div class="form-field">
    <UploadFormHeader :label="label" v-if="description" :description="description" :is-required="required" />
    <FormKit
      type="list"
      :name="name"
      :label="label"
      :validation="validation"
      :validation-label="validationLabel"
      v-model="existingElements"
    >
      <FormKit type="group" v-for="id in listOfElementIds" :key="id">
        <div :data-test="dataTestSubForm" class="formListSection">
          <em :data-test="dataTestRemoveButton" @click="removeItem(id)" class="material-icons close-section">close</em>
          <component :is="subFormComponent" @fieldSpecificDocumentsUpdated="fieldSpecificDocumentsUpdated" />
        </div>
      </FormKit>
      <PrimeButton
        :data-test="dataTestAddButton"
        :label="labelAddButton"
        class="p-button-text"
        icon="pi pi-plus"
        @click="addItem"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from '@formkit/vue';
import PrimeButton from 'primevue/button';
import { defineComponent } from 'vue';
import ProductFormElement from '@/components/forms/parts/elements/derived/ProductFormElement.vue';
import AlignedActivitiesFormElements from '@/components/forms/parts/elements/derived/AlignedActivitiesFormElements.vue';
import NonAlignedActivitiesFormElement from '@/components/forms/parts/elements/derived/NonAlignedActivitiesFormElement.vue';
import ProductionSiteFormElement from '@/components/forms/parts/elements/derived/ProductionSiteFormElement.vue';
import RiskAssessmentFormElement from '@/components/forms/parts/elements/derived/RiskAssessmentFormElement.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import StringBaseDataPointFormField from '@/components/forms/parts/fields/StringBaseDataPointFormField.vue';
import BaseDataPointFormField from '@/components/forms/parts/elements/basic/BaseDataPointFormField.vue';
import { type DocumentToUpload } from '@/utils/FileUploadUtils';
import GeneralViolationsAssessmentFormElement from '@/components/forms/parts/elements/derived/GeneralViolationsAssessmentFormElement.vue';
import GrievanceMechanismAssessmentFormElement from '@/components/forms/parts/elements/derived/GrievanceMechanismAssessmentFormElement.vue';
import PollutionEmissionFormElement from '@/components/forms/parts/elements/derived/PollutionEmissionFormElement.vue';
import SubsidiaryFormElement from '@/components/forms/parts/elements/derived/SubsidiaryFormElement.vue';
import WasteClassificationFormElement from '@/components/forms/parts/elements/derived/WasteClassificationFormElement.vue';
import SiteAndAreaFormElement from '@/components/forms/parts/elements/derived/SiteAndAreaFormElement.vue';
import EmployeesPerCountryFormElement from '@/components/forms/parts/elements/derived/EmployeesPerCountryFormElement.vue';

export default defineComponent({
  name: 'FormListFormField',
  components: {
    BaseDataPointFormField,
    UploadFormHeader,
    ProductFormElement,
    ProductionSiteFormElement,
    RiskAssessmentFormElement,
    GrievanceMechanismAssessmentFormElement,
    GeneralViolationsAssessmentFormElement,
    AlignedActivitiesFormElements,
    NonAlignedActivitiesFormElement,
    StringBaseDataPointFormField,
    FormKit,
    PrimeButton,
    PollutionEmissionFormElement,
    SubsidiaryFormElement,
    WasteClassificationFormElement,
    SiteAndAreaFormElement,
    EmployeesPerCountryFormElement,
  },
  data() {
    return {
      existingElements: [] as unknown[],
      listOfElementIds: (this.displayOneSubFormPerDefault ? [0] : []) as number[],
      idCounter: 0,
    };
  },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    subFormComponent: {
      type: String,
      required: true,
    },
    displayOneSubFormPerDefault: {
      type: Boolean,
      default: false,
    },
    dataTestAddButton: {
      type: String,
      default: 'addButton',
    },
    labelAddButton: {
      type: String,
      default: 'ADD NEW',
    },
    dataTestRemoveButton: {
      type: String,
      default: 'removeButton',
    },
    dataTestSubForm: {
      type: String,
      default: 'subForm',
    },
  },
  mounted() {
    for (let i = this.displayOneSubFormPerDefault ? 1 : 0; i < this.existingElements.length; i++) {
      this.addItem();
    }
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
     * Adds a new Object to the array
     */
    addItem() {
      this.idCounter++;
      this.listOfElementIds.push(this.idCounter);
    },

    /**
     * Remove Object from array
     * @param id - the id of the object in the array
     */
    removeItem(id: number) {
      this.listOfElementIds = this.listOfElementIds.filter((el) => el !== id);
    },
  },
});
</script>
