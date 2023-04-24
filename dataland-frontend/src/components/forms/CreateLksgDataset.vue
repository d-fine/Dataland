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
              <FormKit type="group" name="social" label="social">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">
                      {{ lksgSubAreasNameMappings._general }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                    <p>Please input all relevant basic information about the dataset</p>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="general" :label="lksgSubAreasNameMappings._general">
                      <DateFormField
                        data-test="lksgDataDate"
                        name="dataDate"
                        :display-name="lksgKpisNameMappings.dataDate"
                        :info="lksgKpisInfoMappings.dataDate"
                        validation="required"
                        today-as-max
                      />

                      <div class="form-field" data-test="lksgInScope">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.lksgInScope"
                          :explanation="lksgKpisInfoMappings.lksgInScope"
                          :is-required="true"
                        />
                        <FormKit
                          type="radio"
                          :validation-label="lksgKpisNameMappings.lksgInScope"
                          name="lksgInScope"
                          :options="['Yes', 'No']"
                          :outer-class="{
                            'yes-no-radio': true,
                          }"
                          :inner-class="{
                            'formkit-inner': false,
                          }"
                          :input-class="{
                            'formkit-input': false,
                            'p-radiobutton': true,
                          }"
                          validation="required"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.vatIdentificationNumber"
                          :explanation="lksgKpisInfoMappings.vatIdentificationNumber"
                          :is-required="true"
                        />
                        <FormKit
                          type="text"
                          :validation-label="lksgKpisNameMappings.vatIdentificationNumber"
                          validation="required|length:3"
                          name="vatIdentificationNumber"
                          :inner-class="{ short: true }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.numberOfEmployees"
                          :explanation="lksgKpisInfoMappings.numberOfEmployees"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          name="numberOfEmployees"
                          :validation-label="lksgKpisNameMappings.numberOfEmployees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                          :inner-class="{ short: true }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.shareOfTemporaryWorkers"
                          :explanation="lksgKpisInfoMappings.shareOfTemporaryWorkers"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          name="shareOfTemporaryWorkers"
                          :validation-label="lksgKpisNameMappings.shareOfTemporaryWorkers"
                          placeholder="Value %"
                          step="0.01"
                          min="0"
                          validation="required|number|between:0,100"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.totalRevenue"
                          :explanation="lksgKpisInfoMappings.totalRevenue"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          min="0"
                          :validation-label="lksgKpisNameMappings.totalRevenue"
                          validation="required|number|min:0"
                          name="totalRevenue"
                          placeholder="Value"
                          step="1"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.totalRevenueCurrency"
                          :explanation="lksgKpisInfoMappings.totalRevenueCurrency"
                          :is-required="true"
                        />
                        <FormKit
                          type="text"
                          name="totalRevenueCurrency"
                          :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                          placeholder="Currency"
                          validation="required"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field" data-test="IsYourCompanyManufacturingCompany">
                        <UploadFormHeader
                          :name="'Is your company a manufacturing company?'"
                          :explanation="lksgKpisInfoMappings.listOfProductionSites"
                          :is-required="true"
                        />
                        <FormKit
                          type="radio"
                          :ignore="true"
                          id="IsYourCompanyManufacturingCompany"
                          name="IsYourCompanyManufacturingCompany"
                          :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                          :options="['Yes', 'No']"
                          v-model="isYourCompanyManufacturingCompany"
                          validation="required"
                          :outer-class="{
                            'yes-no-radio': true,
                          }"
                          :inner-class="{
                            'formkit-inner': false,
                          }"
                          :input-class="{
                            'formkit-input': false,
                            'p-radiobutton': true,
                          }"
                        />
                      </div>

                      <FormKit
                        type="list"
                        v-if="isYourCompanyManufacturingCompany !== 'No'"
                        :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                        name="listOfProductionSites"
                        label="listOfProductionSites"
                      >
                        <FormKit type="group" v-for="(item, index) in listOfProductionSites" :key="item.id">
                          <div
                            data-test="productionSiteSection"
                            class="productionSiteSection"
                            :class="isYourCompanyManufacturingCompany === 'No' ? 'p-disabled' : ''"
                          >
                            <em
                              data-test="removeItemFromlistOfProductionSites"
                              @click="removeItemFromlistOfProductionSites(item.id)"
                              class="material-icons close-section"
                              >close</em
                            >

                            <div class="form-field">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.productionSiteName"
                                :explanation="lksgKpisInfoMappings.productionSiteName"
                                :is-required="true"
                              />
                              <FormKit
                                type="text"
                                :validation-label="lksgKpisNameMappings.productionSiteName"
                                name="name"
                                validation="required"
                              />
                            </div>

                            <div class="form-field" data-test="isInHouseProductionOrIsContractProcessing">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.inHouseProductionOrContractProcessing"
                                :explanation="lksgKpisInfoMappings.inHouseProductionOrContractProcessing"
                                :is-required="true"
                              />
                              <FormKit
                                type="radio"
                                name="isInHouseProductionOrIsContractProcessing"
                                :validation-label="lksgKpisNameMappings.inHouseProductionOrContractProcessing"
                                :options="isInHouseProductionOrContractProcessingMap"
                                validation="required"
                                :outer-class="{
                                  'yes-no-radio': true,
                                }"
                                :inner-class="{
                                  'formkit-inner': false,
                                }"
                                :input-class="{
                                  'formkit-input': false,
                                  'p-radiobutton': true,
                                }"
                              />
                            </div>

                            <div class="form-field">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.addressesOfProductionSites"
                                :explanation="lksgKpisInfoMappings.addressesOfProductionSites"
                                :is-required="true"
                              />

                              <FormKit
                                type="text"
                                name="streetAndHouseNumber"
                                validation="required"
                                :validation-label="lksgKpisNameMappings.addressesOfProductionSites"
                                placeholder="Street, House number"
                              />
                              <div class="next-to-each-other">
                                <FormKit
                                  type="select"
                                  name="country"
                                  validation-label="Country"
                                  validation="required"
                                  placeholder="Country"
                                  :options="allCountry"
                                />
                                <FormKit
                                  type="text"
                                  name="city"
                                  validation-label="City"
                                  validation="required"
                                  placeholder="City"
                                />
                                <FormKit
                                  type="text"
                                  validation="required"
                                  validation-label="Postcode"
                                  name="postalCode"
                                  placeholder="postalCode"
                                />
                              </div>
                            </div>

                            <div class="form-field">
                              <div class="form-field-label">
                                <h5>List Of Goods Or Services</h5>
                                <em
                                  class="material-icons info-icon"
                                  aria-hidden="true"
                                  title="listOfGoodsOrServices"
                                  v-tooltip.top="{
                                    value: lksgKpisInfoMappings['listOfGoodsOrServices']
                                      ? lksgKpisInfoMappings['listOfGoodsOrServices']
                                      : '',
                                  }"
                                  >info</em
                                >
                                <PrimeButton
                                  :disabled="listOfProductionSites[index].listOfGoodsOrServicesString === ''"
                                  @click="addNewItemsTolistOfProductionSites(index)"
                                  label="Add"
                                  class="p-button-text"
                                  icon="pi pi-plus"
                                ></PrimeButton>
                              </div>
                              <FormKit
                                data-test="listOfGoodsOrServices"
                                type="text"
                                :ignore="true"
                                v-model="listOfProductionSites[index].listOfGoodsOrServicesString"
                                placeholder="Add comma (,) for more than one value"
                              />
                              <FormKit
                                v-model="listOfProductionSites[index].listOfGoodsOrServices"
                                type="list"
                                label="list of goods or services"
                                name="listOfGoodsOrServices"
                              />
                              <div class="">
                                <span
                                  class="form-list-item"
                                  :key="element"
                                  v-for="element in item.listOfGoodsOrServices"
                                >
                                  {{ element }}
                                  <em
                                    @click="removeItemFromlistOfGoodsOrServices(index, element)"
                                    class="material-icons"
                                    >close</em
                                  >
                                </span>
                              </div>
                            </div>
                          </div>
                        </FormKit>
                        <PrimeButton
                          data-test="ADD-NEW-Production-Site-button"
                          label="ADD NEW Production Site"
                          class="p-button-text"
                          :disabled="isYourCompanyManufacturingCompany === 'No'"
                          icon="pi pi-plus"
                          @click="addNewProductionSite"
                        />
                      </FormKit>
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <FormKit
                type="group"
                v-for="section in lksgDataModel"
                :key="section"
                :label="section.label"
                :name="section.name"
              >
                <div class="uploadFormSection grid" v-for="subsection in section.categories" :key="subsection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 class="anchor title">{{ subsection.label }}</h4>
                    <div :class="`p-badge badge-${section.color}`">
                      <span>{{ section.label.toUpperCase() }}</span>
                    </div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit v-for="field in subsection.fields" :key="field" type="group" :name="subsection.name">
                      <component
                        v-if="isYes(field.dependency)"
                        :is="field.component"
                        :displayName="field.label"
                        :info="field.description"
                        :name="field.name"
                        :placeholder="field.placeholder"
                        :options="field.options"
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
            <li><a @click="smoothScroll('#general')">General</a></li>
            <li><a @click="smoothScroll('#childLabour')">Child labour</a></li>
            <li>
              <a @click="smoothScroll('#forcedLabourSlaveryAndDebtBondage')">Forced labour, slavery and debt bondage</a>
            </li>
            <li>
              <a @click="smoothScroll('#evidenceCertificatesAndAttestations')"
                >Evidence, certificates and attestations</a
              >
            </li>
            <li><a @click="smoothScroll('#grievanceMechanism')">Grievance mechanism</a></li>
            <li><a @click="smoothScroll('#osh')">OSH</a></li>
            <li><a @click="smoothScroll('#freedomOfAssociation')">Freedom of association</a></li>
            <li><a @click="smoothScroll('#humanRights')">Human rights</a></li>
            <li><a @click="smoothScroll('#socialAndEmployeeMatters')">Social and employee matters</a></li>
            <li><a @click="smoothScroll('#environment')">Environment</a></li>
            <li><a @click="smoothScroll('#riskManagement')">Risk management</a></li>
            <li><a @click="smoothScroll('#codeOfConduct')">Code of Conduct</a></li>
            <li><a @click="smoothScroll('#waste')">Waste</a></li>
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
import {
  lksgKpisInfoMappings,
  lksgKpisNameMappings,
  lksgSubAreasNameMappings,
  lksgSubAreas,
  lksgFieldComponentTypes,
  lksgDataModel,
} from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { AxiosError } from "axios";
import { humanizeString } from "@/utils/StringHumanizer";
import { CompanyAssociatedDataLksgData, InHouseProductionOrContractProcessing } from "@clients/backend";
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

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: {
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
      lksgKpisInfoMappings,
      lksgKpisNameMappings,
      lksgSubAreasNameMappings,
      lksgSubAreas,
      lksgFieldComponentTypes,
      lksgDataModel,
      elementPosition: 0,
      scrollListener: (): null => null,
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
     * Returns the value of a given YesNo variable
     *
     * @param variable the string representation of the YesNo variable to be read out
     * @returns either "Yes" or "No"
     */
    getYesNoValue(variable: string | undefined): string {
      if (variable == undefined || variable == "") {
        return "Yes";
      }
      return eval(variable) as string;
    },
    /**
     * Returns the value of a given YesNo variable is Yes
     *
     * @param variable the string representation of the YesNo variable to be read out
     * @returns the boolean result
     */
    isYes(variable: string | undefined): boolean {
      if (variable == undefined || variable == "") {
        return true;
      }
      return eval(variable) === "Yes";
    },
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
      const numberOfProductionSites = lksgDataset.data?.general?.listOfProductionSites?.length || 0;
      if (numberOfProductionSites > 0) {
        this.isYourCompanyManufacturingCompany = "Yes";
        const productionSites = assertDefined(lksgDataset.data?.general?.listOfProductionSites);
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
     *
     * @param id - the id of the object in the array
     */
    removeItemFromlistOfProductionSites(id: number) {
      this.listOfProductionSites = this.listOfProductionSites.filter((el) => el.id !== id);
    },

    /**
     * Adds a new item to the list of Production Sites Goods Or Services
     *
     * @param index - index of the element in the listOfProductionSites array
     */
    addNewItemsTolistOfProductionSites(index: number) {
      const items = this.listOfProductionSites[index].listOfGoodsOrServicesString.split(";").map((item) => item.trim());
      this.listOfProductionSites[index].listOfGoodsOrServices = [
        ...this.listOfProductionSites[index].listOfGoodsOrServices,
        ...items,
      ];
      this.listOfProductionSites[index].listOfGoodsOrServicesString = "";
    },

    /**
     * Remove item from list of Production Sites Goods Or Services
     *
     * @param index - index of the element in the listOfProductionSites array
     * @param item - which item is to be deleted
     */
    removeItemFromlistOfGoodsOrServices(index: number, item: string) {
      this.listOfProductionSites[index].listOfGoodsOrServices = this.listOfProductionSites[
        index
      ].listOfGoodsOrServices.filter((el) => el !== item);
    },
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 100px;
}
</style>
