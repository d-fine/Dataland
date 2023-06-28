<template>
  <div data-test="productFormElement">
    <div class="form-field">
      <InputTextFormField name="productName" label="Product Name" description="Please enter the name of the product" />
    </div>
    <div class="form-field">
      <div class="flex justify-content-between">
        <UploadFormHeader
          label="Production Steps"
          description="Please give a brief overview of the production steps/activities undertaken"
        />
        <PrimeButton
          :disabled="listOfProductionStepsString === ''"
          @click="addNewItemsToListOfProductionSteps()"
          data-test="addProductionStep"
          label="Add"
          class="p-button-text"
          icon="pi pi-plus"
        ></PrimeButton>
      </div>

      <FormKit
        :data-test="`listOfProductionSteps${id}`"
        type="text"
        :ignore="true"
        v-model="listOfProductionStepsString"
        placeholder="Add comma (,) for more than one value"
      />
      <FormKit
        v-if="listOfProductionStepsString.length > 0"
        type="text"
        v-model="listOfProductionStepsString"
        validation="length:0,0"
        validation-visibility="live"
        :validation-messages="{ length: 'Please add the entered value via pressing the add button or empty the field.' }"
        outer-class="hidden-input"
      />

      <FormKit v-model="listOfProductionSteps" type="list" label="list of production steps" name="productionSteps" />
      <div class="">
        <span class="form-list-item" :key="element" v-for="element in listOfProductionSteps">
          {{ element }}
          <em @click="removeItemFromListOfProductionSteps(element)" class="material-icons">close</em>
        </span>
      </div>
    </div>

    <div class="form-field">
      <UploadFormHeader
        label="Related Corporate Supply Chain"
        description="Please give an overview of the related corporate supply chain(s) and key business relationships (by procurement or order volume) (own operations)"
      />
      <FreeTextFormField name="relatedCorporateSupplyChain" v-model="existingRelatedCorporateSupplyChain" />
    </div>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";

export default defineComponent({
  name: "ProductFormElement",
  components: {
    UploadFormHeader,
    InputTextFormField,
    FormKit,
    PrimeButton,
    FreeTextFormField,
  },
  data() {
    return {
      existingRelatedCorporateSupplyChain: "",
      listOfProductionStepsString: "",
      listOfProductionSteps: [] as string[],
    };
  },
  props: {
    id: {
      type: String,
      required: true,
    },
  },
  methods: {
    /**
     * Remove item from list of Production Steps
     * @param element - the item to be deleted
     */
    removeItemFromListOfProductionSteps(element: string) {
      if (this.listOfProductionSteps) {
        this.listOfProductionSteps = this.listOfProductionSteps.filter((el) => el !== element);
      }
    },

    /**
     * Adds a new item to the list of Production Steps
     */
    addNewItemsToListOfProductionSteps() {
      const items = this.listOfProductionStepsString
        .split(",")
        .map((element) => element.trim())
        .filter((element) => element !== "");
      this.listOfProductionSteps = [...new Set([...this.listOfProductionSteps, ...items])];
      this.listOfProductionStepsString = "";
    },
  },
});
</script>
