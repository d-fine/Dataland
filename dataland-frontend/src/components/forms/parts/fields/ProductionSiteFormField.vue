<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <FormKit type="list" :name="name" :label="displayName">
      <FormKit type="group" v-for="item in listOfProductionSites" :key="item.id">
        <div data-test="productionSiteSection" class="productionSiteSection">
          <em
            data-test="removeItemFromListOfProductionSites"
            @click="removeItemFromListOfProductionSites(item.id)"
            class="material-icons close-section"
            >close</em
          >
          <ProductionSiteFormElement />
        </div>
      </FormKit>
      <PrimeButton
        data-test="ADD-NEW-Production-Site-button"
        label="ADD NEW Production Site"
        class="p-button-text"
        icon="pi pi-plus"
        @click="addNewProductionSite"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { LksgProductionSite } from "@clients/backend";
import ProductionSiteFormElement from "@/components/forms/parts/elements/derived/ProductionSiteFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "ProductionSiteFormField",
  props: {
    name: {
      type: String,
      required: true,
    },
    info: {
      type: String,
      default: "",
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    lksgDataModel() {
      return lksgDataModel;
    },
  },
  data() {
    return {
      allCountry: getAllCountryNamesWithCodes(),
      productionSite: Object as LksgProductionSite,
      listOfGoodsOrServicesString: "",
      listOfProductionSites: [
        {
          id: 0,
          listOfGoodsOrServices: [] as string[],
          listOfGoodsOrServicesString: "",
        },
      ],
      idCounter: 0,
    };
  },
  components: {
    UploadFormHeader,
    ProductionSiteFormElement,
    FormKit,
    PrimeButton,
  },
  methods: {
    /**
     * Adds a new Object to the ProductionSite array
     */
    addNewProductionSite() {
      this.idCounter++;
      this.listOfProductionSites.push({
        id: this.idCounter,
        listOfGoodsOrServices: [],
        listOfGoodsOrServicesString: "",
      });
    },

    /**
     * Remove Object from ProductionSite array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfProductionSites(id: number) {
      this.listOfProductionSites = this.listOfProductionSites.filter((el) => el.id !== id);
    },
  },
});
</script>
