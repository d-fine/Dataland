<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
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
          <component :is="subFormComponent" />
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
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import ProductFormElement from "@/components/forms/parts/elements/derived/ProductFormElement.vue";
import AlignedActivitiesFormElements from "@/components/forms/parts/elements/derived/AlignedActivitiesFormElements.vue";
import ProductionSiteFormElement from "@/components/forms/parts/elements/derived/ProductionSiteFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "FormListFormField",
  components: {
    UploadFormHeader,
    ProductFormElement,
    ProductionSiteFormElement,
    FormKit,
    PrimeButton,
    AlignedActivitiesFormElements,
  },
  data() {
    return {
      existingElements: [] as unknown[],
      listOfElementIds: (this.displayOneSubFormPerDefault ? [0] : []) as number[],
      idCounter: 0,
    };
  },
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
      default: "addButton",
    },
    labelAddButton: {
      type: String,
      default: "ADD NEW",
    },
    dataTestRemoveButton: {
      type: String,
      default: "removeButton",
    },
    dataTestSubForm: {
      type: String,
      default: "subForm",
    },
  },
  mounted() {
    for (let i = this.displayOneSubFormPerDefault ? 1 : 0; i < this.existingElements.length; i++) {
      this.addItem();
    }
  },
  methods: {
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
