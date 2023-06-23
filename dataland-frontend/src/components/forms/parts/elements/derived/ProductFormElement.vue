<template>
  <div class="form-field">
    <InputTextFormField name="productName" label="Product Name" description="Please enter the name of the product" />
  </div>
  <div class="form-field">
    <UploadFormHeader
      label="Production Steps"
      description="Please give a brief overview of the production steps/activities undertaken"
    />
  </div>

  <div class="form-field">
    <UploadFormHeader
      label="Related Corporate Supply Chain"
      description="Please give an overview of the related corporate supply chain(s) and key business relationships (by procurement or order volume) (own operations)"
    />
    <FormKit type="list" name="relatedCorporateSupplyChain" v-model="existingRelatedCorporateSupplyChain">
      <div v-for="id in listOfRelatedCorporateSupplyChainIds" :key="id">
        <div data-test="relatedCorporateSupplyChainSection" class="relatedCorporateSupplyChainSection flex vertical-middle align-content-center">
          <em
            data-test="removeItemFromListOfrelatedCorporateSupplyChain"
            @click="removeItemFromListOfrelatedCorporateSupplyChain(id)"
            class="material-icons mr-2"
          >
            close
          </em>
          <FormKit type="text" class="static" />
        </div>
      </div>
      <PrimeButton
        data-test="ADD-NEW-Related-Corporate-Supply-Chain-button"
        label="ADD NEW Related Corporate Supply Chain"
        class="p-button-text"
        icon="pi pi-plus"
        @click="addNewRelatedCorporateSupplyChain"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "ProductFormElement",
  components: {
    UploadFormHeader,
    InputTextFormField,
    FormKit,
    PrimeButton,
  },
  data() {
    return {
      existingRelatedCorporateSupplyChain: [] as string[],
      listOfRelatedCorporateSupplyChainIds: [] as number[],
      productionStepIdCounter: 0,
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
     * Adds a new Object to the Product array
     */
    addNewRelatedCorporateSupplyChain() {
      this.productionStepIdCounter++;
      this.listOfRelatedCorporateSupplyChainIds.push(this.productionStepIdCounter);
    },

    /**
     * Remove Object from Product array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfrelatedCorporateSupplyChain(id: number) {
      this.listOfRelatedCorporateSupplyChainIds = this.listOfRelatedCorporateSupplyChainIds.filter((el) => el !== id);
    },
  },
});
</script>
