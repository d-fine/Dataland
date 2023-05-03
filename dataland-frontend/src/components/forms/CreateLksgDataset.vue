<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - LkSG</template>
    <template #content>
      <div v-show="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading LkSG data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-show="!waitingForData" class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedLksgData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postLkSGData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID!" disabled="true" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" disabled="true" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in lksgDataModel"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="uploadFormSection grid" v-for="subcategory in category.subcategories" :key="subcategory">
                  <div class="col-3 p-3 topicLabel">
                    <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                    <div :class="`p-badge badge-${category.color}`">
                      <span>{{ category.label.toUpperCase() }}</span>
                    </div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                      <component
                        v-if="field.showIf(companyAssociatedLksgData.data)"
                        :is="field.component"
                        :displayName="field.label"
                        :placeholder="field.placeholder"
                        :info="field.description"
                        :name="field.name"
                        :options="field.options"
                        :required="field.required"
                      />
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postLkSGDataProcessed">
            <SuccessUpload v-if="uploadSucceded" :messageId="messageCounter" />
            <FailedUpload v-else :message="message" :messageId="messageCounter" />
          </div>
          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in lksgDataModel" :key="category">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory">
                  <a @click="smoothScroll(`#${subcategory.name}`)">{{ subcategory.label }}</a>
                </li>
              </ul>
            </li>
          </ul>
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Tooltip from "primevue/tooltip";
import PrimeButton from "primevue/button";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import Calendar from "primevue/calendar";
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { AxiosError } from "axios";
import { CompanyAssociatedDataLksgData } from "@clients/backend";
import { useRoute } from "vue-router";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import { smoothScroll } from "@/utils/smoothScroll";
import { checkCustomInputs } from "@/utils/validationsUtils";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import DateFormField from "@/components/forms/parts/fields/DateFormField.vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
import YesNoNaFormField from "@/components/forms/parts/fields/YesNoNaFormField.vue";
import ProductionSiteFormField from "@/components/forms/parts/fields/ProductionSiteFormField.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: {
    ProductionSiteFormField,
    UploadFormHeader,
    SuccessUpload,
    FailedUpload,
    FormKit,
    Card,
    PrimeButton,
    Calendar,
    YesNoFormField,
    InputTextFormField,
    FreeTextFormField,
    NumberFormField,
    DateFormField,
    SingleSelectFormField,
    MultiSelectFormField,
    NaceCodeFormField,
    AddressFormField,
    RadioButtonsFormField,
    YesNoNaFormField,
    SubmitButton,
    SubmitSideBar,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createLkSGForm",
      isYourCompanyManufacturingCompany: "No",
      listOfProductionSites: [
        {
          id: 0,
          listOfGoodsOrServices: [] as string[],
          listOfGoodsOrServicesString: "",
        },
      ],
      idCounter: 0,
      allCountry: getAllCountryNamesWithCodes(),
      waitingForData: false,
      dataDate: undefined as Date | undefined,
      companyAssociatedLksgData: {} as CompanyAssociatedDataLksgData,
      route: useRoute(),
      message: "",
      uploadSucceded: false,
      postLkSGDataProcessed: false,
      messageCounter: 0,
      lksgDataModel,
      elementPosition: 0,
      scrollListener: (): null => null,
      smoothScroll,
      checkCustomInputs,
      updatingData: false,
    };
  },
  computed: {
    yearOfDataDate(): string {
      return this.dataDate?.getFullYear()?.toString() || "";
    },
    convertedDataDate(): string {
      if (this.dataDate) {
        return getHyphenatedDate(this.dataDate);
      } else {
        return "";
      }
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  mounted() {
    const dataId = this.route.query.templateDataId;
    if (dataId !== undefined && typeof dataId === "string" && dataId !== "") {
      void this.loadLKSGData(dataId);
    }
  },
  methods: {
    /**
     * Loads the LkSG-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     *
     * @param dataId the id of the dataset to load
     */
    async loadLKSGData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const lkSGDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getLksgDataControllerApi();

      const dataResponse = await lkSGDataControllerApi.getCompanyAssociatedLksgData(dataId);
      const lksgDataset = dataResponse.data;
      const numberOfProductionSites = lksgDataset.data?.general?.productionSpecific?.listOfProductionSites?.length || 0;
      if (numberOfProductionSites > 0) {
        this.isYourCompanyManufacturingCompany = "Yes";
        const productionSites = assertDefined(lksgDataset.data?.general?.productionSpecific?.listOfProductionSites);
        this.listOfProductionSites = [];
        this.idCounter = numberOfProductionSites;
        for (let i = 0; i < numberOfProductionSites; i++) {
          this.listOfProductionSites.push({
            id: i,
            listOfGoodsOrServices: productionSites[i].listOfGoodsOrServices || [],
            listOfGoodsOrServicesString: "",
          });
        }
      }
      const dataDateFromDataset = lksgDataset.data?.general?.masterData?.dataDate;
      if (dataDateFromDataset) {
        this.dataDate = new Date(dataDateFromDataset);
      }
      this.companyAssociatedLksgData = lksgDataset;
      this.waitingForData = false;
    },
    /**
     * Sends data to add LkSG data
     */
    async postLkSGData(): Promise<void> {
      this.messageCounter++;
      try {
        const lkSGDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        await lkSGDataControllerApi.postCompanyAssociatedLksgData(this.companyAssociatedLksgData);
        this.$emit("datasetCreated");
        this.$formkit.reset(this.formId);
        this.isYourCompanyManufacturingCompany = "No";
        this.listOfProductionSites = [
          {
            id: 0,
            listOfGoodsOrServices: [],
            listOfGoodsOrServicesString: "",
          },
        ];
        this.idCounter = 0;
        this.dataDate = undefined;
        this.message = "Upload successfully executed.";
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = "An error occurred: " + error.message;
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        }
        this.uploadSucceded = false;
      } finally {
        this.postLkSGDataProcessed = true;
      }
    },
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 100px;
}
</style>
