<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - Nuclear & Gas</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading Nuclear & Gas data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
              v-model="companyAssociatedNuclearAndGasData"
              :actions="false"
              type="form"
              :id="formId"
              :name="formId"
              @submit="postNuclearandGasData"
              @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <div class="uploadFormSection grid">
              <div class="col-3 p-3 topicLabel">
                <h4 id="reportingPeriod" class="anchor title">Reporting Period</h4>
              </div>
              <div class="col-9 form-field formFields uploaded-files">
                <UploadFormHeader
                    :label="'Reporting Period'"
                    :description="'The reporting period the dataset belongs to (e.g. a fiscal year).'"
                    :is-required="true"
                />
                <div class="lg:col-4 md:col-6 col-12 pl-0">
                  <Calendar
                      data-test="reportingPeriod"
                      v-model="reportingPeriod"
                      inputId="icon"
                      :showIcon="true"
                      view="year"
                      dateFormat="yy"
                      validation="required"
                  />
                </div>

                <FormKit type="hidden" :modelValue="reportingPeriodYear.toString()" name="reportingPeriod" />
              </div>
            </div>

            <FormKit type="group" name="data" label="data">
              <div
                  v-for="category in nuclearAndGasDataModel"
                  :key="category.name"
                  :label="category.label"
                  :name="category.name"
              >
                <div class="uploadFormSection grid"
                     v-for="subcategory in category.subcategories"
                     :key="subcategory.name"
                >
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="col-3 p-3 topicLabel">
                      <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                      <div :class="`p-badge badge-${category.color}`">
                        <span>{{ category.label.toUpperCase() }}</span>
                      </div>
                    </div>
                    <div class="col-9 formFields">
                      <FormKit
                          v-for="field in subcategory.fields"
                          :key="field"
                          type="group"
                          :name="subcategory.name"
                      >
                        <component
                            v-if="field.showIf(companyAssociatedNuclearAndGasData.data)"
                            :is="field.component"
                            :label="field.label"
                            :placeholder="field.placeholder"
                            :description="field.description"
                            :name="field.name"
                            :options="field.options"
                            :required="field.required"
                            :validation="field.validation"
                            :validation-label="field.validationLabel"
                            :data-test="field.name"
                            :unit="field.unit"
                            @reportsUpdated="updateReportsSelection"
                            :ref="field.name"
                        />
                      </FormKit>
                    </div>
                  </template>
                </div>
              </div>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postNuclearAndGasDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in nuclearAndGasDataModel" :key="category.name">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory.name">
                  <a
                    v-if="subcategoryVisibility.get(subcategory) ?? true"
                    @click="smoothScroll(`#${subcategory.name}`)"
                    >{{ subcategory.label }}</a
                  >
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
// @ts-nocheck
import { FormKit } from '@formkit/vue';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import { defineComponent, inject, computed } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import Tooltip from 'primevue/tooltip';
import PrimeButton from 'primevue/button';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import Calendar from 'primevue/calendar';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { nuclearAndGasDataModel } from '@/frameworks/nuclear-and-gas/UploadConfig';
import {
  CompanyAssociatedDataNuclearAndGasData, type CompanyReport,
  DataTypeEnum, NuclearAndGasData,
} from '@clients/backend';
import { useRoute } from 'vue-router';
import {checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel} from '@/utils/ValidationUtils';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import YesNoNaFormField from '@/components/forms/parts/fields/YesNoNaFormField.vue';
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import { type Subcategory } from '@/utils/GenericFrameworkTypes';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import HighImpactClimateSectorsFormField from '@/components/forms/parts/fields/HighImpactClimateSectorsFormField.vue';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import BaseDataPointFormField from '@/components/forms/parts/elements/basic/BaseDataPointFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';
import {DocumentToUpload, uploadFiles} from "@/utils/FileUploadUtils";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateNuclearandGasDataset',
  components: {
    BaseDataPointFormField,
    SubmitButton,
    SubmitSideBar,
    UploadFormHeader,
    SuccessMessage,
    FailMessage,
    FormKit,
    Card,
    PrimeButton,
    Calendar,
    InputTextFormField,
    FreeTextFormField,
    NumberFormField,
    DateFormField,
    SingleSelectFormField,
    MultiSelectFormField,
    NaceCodeFormField,
    AddressFormField,
    RadioButtonsFormField,
    PercentageFormField,
    ProductionSitesFormField,
    MostImportantProductsFormField,
    ProcurementCategoriesFormField,
    UploadReports,
    HighImpactClimateSectorsFormField,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyDataPointFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createNuclearAndGasForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedNuclearAndGasData: {} as CompanyAssociatedDataNuclearAndGasData,
      nuclearAndGasDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postNuclearAndGasDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      documentsToUpload: [] as DocumentToUpload[],
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      reportingPeriod: undefined as undefined | Date,
      listOfFilledKpis: [] as Array<string>,
    };
  },
  computed: {
    reportingPeriodYear(): number {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear();
      }
      return 0;
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.nuclearAndGasDataModel, this.companyAssociatedNuclearAndGasData.data);
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  created() {
    const dataId = this.route.query.templateDataId;
    if (dataId && typeof dataId === 'string') {
      void this.loadNuclearAndGasData(dataId);
    } else {
      this.waitingForData = false;
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }
  },
  methods: {
    /**
     * Builds an api to get and upload Nuclear and Gas data
     * @returns the api
     */
    buildNuclearAndGasDataApi(): PublicFrameworkDataApi<NuclearAndGasData> {
      console.log('bin in buildNuclearAndGasDataApi')
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.NuclearAndGas);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      }
    },

    /**
     * Loads the Nuclear-and-Gas-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadNuclearAndGasData(dataId: string): Promise<void> {
      console.log('bin in loadNuclearAndGasData')
      this.waitingForData = true;
      const NuclearAndGasDataControllerApi = this.buildNuclearAndGasDataApi();
      const dataResponse = await NuclearAndGasDataControllerApi.getFrameworkData(dataId);
      const NuclearAndGasResponseData = dataResponse.data;
      this.listOfFilledKpis = getFilledKpis(NuclearAndGasResponseData);
      if (NuclearAndGasResponseData?.reportingPeriod) {
        this.reportingPeriod = new Date(NuclearAndGasResponseData.reportingPeriod);
      }
      this.companyAssociatedNuclearAndGasData = objectDropNull(NuclearAndGasResponseData as ObjectType) as CompanyAssociatedDataNuclearAndGasResponseData;
      this.waitingForData = false;
    },
    /**
     * Sends data to add NuclearandGas data
     */
    async postNuclearandGasData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documentsToUpload.length > 0) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
              this.companyAssociatedNuclearAndGasData.data as ObjectType,
              Object.keys(this.namesAndReferencesOfAllCompanyReportsForTheDataset)
          );

          await uploadFiles(this.documentsToUpload, assertDefined(this.getKeycloakPromise));
        }
        const NuclearAndGasDataControllerApi = this.buildNuclearAndGasDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
            this.companyAssociatedNuclearAndGasData.companyId,
            this.getKeycloakPromise
        );

        await NuclearAndGasDataControllerApi.postFrameworkData(
            this.companyAssociatedNuclearAndGasData,
            isCompanyOwnerOrDataUploader);

        this.$emit('datasetCreated');
        this.dataDate = undefined;
        this.message = 'Upload successfully executed.';
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error.message) {
          this.message = formatAxiosErrorMessage(error as Error);
        } else {
          this.message =
              'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
        this.uploadSucceded = false;
      } finally {
        this.postNuclearAndGasDataProcessed = true;
      }
    },
    /**
     * Sets the object containing the names of all stored and to-be-uploaded reports as keys, and their respective
     * fileReferences as values, and then sets the selection of reports that are to be uploaded.
     * @param reportsNamesAndReferences contains the names of all stored and to-be-uploaded reports as keys,
     * and their respective fileReferences as values
     * @param reportsToUpload contains the actual selection of reports that are to be uploaded
     */
    updateReportsSelection(reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]) {
      this.namesAndReferencesOfAllCompanyReportsForTheDataset = reportsNamesAndReferences;
      this.documentsToUpload = [...reportsToUpload];
    },
  },
  provide() {
    return {
        namesAndReferencesOfAllCompanyReportsForTheDataset: computed(() => {
          return this.namesAndReferencesOfAllCompanyReportsForTheDataset;
        }),
        referencedReportsForPrefill: computed(() => {
          return this.referencedReportsForPrefill;
        }),
        listOfFilledKpis: computed(() => {
          return this.listOfFilledKpis;
        }),
      };
    },
});
</script>
