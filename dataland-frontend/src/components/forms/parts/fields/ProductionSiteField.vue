<template>
  <div class="form-field">
    <InputTextFormField
      name="productionSiteName"
      :info="lksgKpisInfoMappings.productionSiteName"
      :display-name="lksgKpisNameMappings.productionSiteName"
      validation="required"
    />
  </div>

  <div class="form-field" data-test="isInHouseProductionOrIsContractProcessing">
    <RadioButtonsFormField
      name="isInHouseProductionOrIsContractProcessing"
      :info="lksgKpisInfoMappings.inHouseProductionOrContractProcessing"
      :display-name="lksgKpisNameMappings.inHouseProductionOrContractProcessing"
      validation="required"
      :options="isInHouseProductionOrContractProcessingOptions"
    />
  </div>

  <div class="form-field">
    <AddressFormField
      name="addressesOfProductionSites"
      :info="lksgKpisInfoMappings.addressesOfProductionSites"
      :display-name="lksgKpisNameMappings.addressesOfProductionSites"
      validation="required"
    />
  </div>

  <div class="form-field">
    <div class="form-field-label">
      <h5>List Of Goods Or Services</h5>
      <em
        class="material-icons info-icon"
        aria-hidden="true"
        title="listOfGoodsOrServices"
        v-tooltip.top="{
          value: lksgKpisInfoMappings['listOfGoodsOrServices'] ? lksgKpisInfoMappings['listOfGoodsOrServices'] : '',
        }"
        >info</em
      >
      <PrimeButton
        :disabled="item.listOfGoodsOrServicesString === ''"
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
      v-model="item.listOfGoodsOrServicesString"
      placeholder="Add comma (,) for more than one value"
    />
    <FormKit
      v-model="item.listOfGoodsOrServices"
      type="list"
      label="list of goods or services"
      name="listOfGoodsOrServices"
    />
    <div class="">
      <span class="form-list-item" :key="element" v-for="element in item.listOfGoodsOrServices">
        {{ element }}
        <em @click="removeItemFromListOfGoodsOrServices(element)" class="material-icons">close</em>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import {
  lksgKpisInfoMappings,
  lksgKpisNameMappings,
  Option,
} from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import { defineComponent, PropType } from "vue";
import { InHouseProductionOrContractProcessing, LksgProductionSite } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { TempLksgProductionSite } from "@/components/forms/parts/TempLksgProductionSite";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";

export default defineComponent({
  name: "ProductionSiteElement",
  computed: {
    lksgKpisNameMappings() {
      return lksgKpisNameMappings;
    },
    lksgKpisInfoMappings() {
      return lksgKpisInfoMappings;
    },
  },
  data() {
    return {
      allCountry: getAllCountryNamesWithCodes(),
      isInHouseProductionOrContractProcessingOptions: [
        {
          label: InHouseProductionOrContractProcessing.InHouseProduction,
          value: humanizeString(InHouseProductionOrContractProcessing.InHouseProduction),
        },
        {
          label: InHouseProductionOrContractProcessing.ContractProcessing,
          value: humanizeString(InHouseProductionOrContractProcessing.ContractProcessing),
        },
      ] as Option[],
      isInHouseProductionOrContractProcessingMap: Object.fromEntries(
        new Map<string, string>([
          [
            InHouseProductionOrContractProcessing.InHouseProduction,
            humanizeString(InHouseProductionOrContractProcessing.InHouseProduction),
          ],
          [
            InHouseProductionOrContractProcessing.ContractProcessing,
            humanizeString(InHouseProductionOrContractProcessing.ContractProcessing),
          ],
        ])
      ),
    };
  },
  components: {
    FreeTextFormField,
    InputTextFormField,
    AddressFormField,
    RadioButtonsFormField,
    UploadFormHeader,
    FormKit,
  },
  props: {
    item: {
      type: Object as PropType<TempLksgProductionSite>,
      required: true,
    },
  },
  methods: {
    /**
     * Remove item from list of Production Sites Goods Or Services
     *
     * @param element - the item to be deleted
     */
    removeItemFromListOfGoodsOrServices(element: string) {
      this.item.listOfGoodsOrServices = this.item.listOfGoodsOrServices.filter((el) => el !== element);
    },

    /**
     * Adds a new item to the list of Production Sites Goods Or Services
     */
    addNewItemsToListOfGoodsOrServices() {
      const items = this.item.listOfGoodsOrServicesString.split(";").map((element) => element.trim());
      this.item.listOfGoodsOrServices = [...this.item.listOfGoodsOrServices, ...items];
      this.item.listOfGoodsOrServicesString = "";
    },
  },
});
</script>
