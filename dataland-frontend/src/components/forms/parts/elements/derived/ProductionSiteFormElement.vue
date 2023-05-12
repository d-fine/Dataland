<template>
  <div class="form-field">
    <InputTextFormField
      name="nameOfProductionSite"
      info="Please state the name of the production site."
      display-name="Production Site"
      validation="required"
    />
  </div>

  <div class="form-field">
    <AddressFormField
      name="addressOfProductionSite"
      info="Please state the address of the production site."
      display-name="Production Site Address"
      validation="required"
    />
  </div>

  <div class="form-field">
    <div class="flex justify-content-between">
      <UploadFormHeader name="Lists of Goods or Services" explanation="Provide List of Goods or Services" />
      <PrimeButton
        :disabled="listOfGoodsOrServicesString === ''"
        @click="addNewItemsToListOfGoodsOrServices()"
        label="Add"
        class="p-button-text"
        icon="pi pi-plus"
      ></PrimeButton>
    </div>

    <FormKit
      data-test="listOfGoodsOrServices"
      type="text"
      :ignore="true"
      v-model="listOfGoodsOrServicesString"
      placeholder="Add comma (,) for more than one value"
      validation="length:0,0"
      :validation-messages="{ length: 'Please add the entered value via pressing the add button or empty the field.' }"
    />
    <FormKit
      v-model="listOfGoodsOrServices"
      type="list"
      label="list of goods or services"
      name="listOfGoodsOrServices"
    />
    <div class="">
      <span class="form-list-item" :key="element" v-for="element in listOfGoodsOrServices">
        {{ element }}
        <em @click="removeItemFromListOfGoodsOrServices(element)" class="material-icons">close</em>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "ProductionSiteFormElement",
  data() {
    return {
      listOfGoodsOrServicesString: "",
      listOfGoodsOrServices: [] as string[],
    };
  },
  components: {
    UploadFormHeader,
    InputTextFormField,
    AddressFormField,
    FormKit,
    PrimeButton,
  },
  methods: {
    /**
     * Remove item from list of Production Sites Goods Or Services
     * @param element - the item to be deleted
     */
    removeItemFromListOfGoodsOrServices(element: string) {
      if (this.listOfGoodsOrServices) {
        this.listOfGoodsOrServices = this.listOfGoodsOrServices.filter((el) => el !== element);
      }
    },

    /**
     * Adds a new item to the list of Production Sites Goods Or Services
     */
    addNewItemsToListOfGoodsOrServices() {
      const items = this.listOfGoodsOrServicesString.split(",").map((element) => element.trim());
      this.listOfGoodsOrServices = [...new Set([...this.listOfGoodsOrServices, ...items])];
    },
  },
});
</script>
