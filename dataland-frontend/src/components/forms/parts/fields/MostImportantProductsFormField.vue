<template>
  <div class="form-field">
    <UploadFormHeader :label="label!" :description="description!" :is-required="required" />
    <FormKit
      type="list"
      :name="name"
      :label="label"
      :validation="validation!"
      :validation-label="validationLabel!"
      v-model="existingProducts"
    >
      <FormKit type="group" v-for="id in listOfProductIds" :key="id">
        <div data-test="productSection" class="productSection">
          <em
            data-test="removeItemFromListOfProducts"
            @click="removeItemFromListOfProducts(id)"
            class="material-icons close-section"
            >close</em
          >
          <ProductFormElement :id="id.toString()" />
        </div>
      </FormKit>
      <PrimeButton
        data-test="ADD-NEW-Product-button"
        label="ADD NEW Product"
        class="p-button-text"
        icon="pi pi-plus"
        @click="addNewProductEventHandler"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { LksgProduct } from "@clients/backend";
import ProductFormElement from "@/components/forms/parts/elements/derived/ProductFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MostImportantProductsFormField",
  props: FormFieldProps,
  data() {
    return {
      existingProducts: [] as LksgProduct[],
      listOfProductIds: [] as number[],
      idCounter: 0,
      userAddedProduct: false,
    };
  },
  components: {
    UploadFormHeader,
    ProductFormElement,
    FormKit,
    PrimeButton,
  },
  mounted() {
    for (let i = 0; i < this.existingProducts.length; i++) {
      this.addNewProduct();
    }
  },
  watch: {
    existingProducts(newValue) {
      if(this.userAddedProduct) {
          return;
      }
      for (let i = 0; i < this.existingProducts.length; i++) {
          this.addNewProduct();
      }
    }
  },
  methods: {
    /**
     * Adds a new Object to the Product array
     */
    addNewProductEventHandler() {
        this.userAddedProduct = true;
        this.addNewProduct()
    },
    /**
     * Adds a new Object to the Product array
     */
    addNewProduct() {
        this.idCounter++;
        this.listOfProductIds.push(this.idCounter);
    },

    /**
     * Remove Object from Product array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfProducts(id: number) {
      this.listOfProductIds = this.listOfProductIds.filter((el) => el !== id);
    },
  },
});
</script>
