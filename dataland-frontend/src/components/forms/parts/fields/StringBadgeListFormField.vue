<template>
  <div class="form-field">
    <div class="flex justify-content-between">
      <UploadFormHeader :label="label" :description="description" />
      <PrimeButton
        :disabled="listOfElementsString === ''"
        @click="addNewItems()"
        :data-test="dataTestAddButton"
        label="Add"
        class="p-button-text"
        icon="pi pi-plus"
      />
    </div>

    <FormKit
      :data-test="dataTestListInput"
      type="text"
      :ignore="true"
      v-model="listOfElementsString"
      placeholder="Add comma (,) for more than one value"
    />
    <FormKit
      v-if="listOfElementsString.length > 0"
      type="text"
      v-model="listOfElementsString"
      validation="length:0,0"
      validation-visibility="live"
      :validation-messages="{
        length: 'Please add the entered value via pressing the add button or empty the field.',
      }"
      outer-class="hidden-input"
    />

    <FormKit v-model="listOfElements" type="list" :name="name" />
    <div class="">
      <span class="form-list-item" :key="element" v-for="element in listOfElements">
        {{ element }}
        <em @click="removeItem(element)" class="material-icons">close</em>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import PrimeButton from 'primevue/button';

export default defineComponent({
  name: 'StringBadgeListFormField',
  components: { PrimeButton, FormKit, UploadFormHeader },
  data() {
    return {
      listOfElementsString: '',
      listOfElements: [] as string[],
    };
  },
  props: {
    ...BaseFormFieldProps,
    dataTestAddButton: {
      type: String,
      default: 'addButton',
    },
    dataTestListInput: {
      type: String,
      default: 'listOfElementsInput',
    },
  },
  methods: {
    /**
     * Remove item from selected elements
     * @param element - the item to be deleted
     */
    removeItem(element: string) {
      if (this.listOfElements) {
        this.listOfElements = this.listOfElements.filter((el) => el !== element);
      }
    },

    /**
     * Add new items to the list of selected elements
     */
    addNewItems() {
      const items = this.listOfElementsString
        .split(',')
        .map((element) => element.trim())
        .filter((element) => element !== '');
      this.listOfElements = [...new Set([...this.listOfElements, ...items])];
      this.listOfElementsString = '';
    },
  },
});
</script>
