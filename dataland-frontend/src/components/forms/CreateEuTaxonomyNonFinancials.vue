<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title
      ><span data-test="pageWrapperTitle">
        {{ editMode ? 'Edit' : 'Create' }} EU Taxonomy Dataset for a Non-Financial Company/Service</span
      ></template
    >
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading Eu Taxonomy For Non Financials data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedEutaxonomyNonFinancialsData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postEutaxonomyNonFinancialsData"
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
                v-for="category in eutaxonomyNonFinancialsDataModel"
                :key="category.name"
                :label="category.label"
                :name="category.name"
              >
                <div
                  class="uploadFormSection grid"
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
                        :key="field.name"
                        type="group"
                        :name="subcategory.name"
                      >
                        <component
                          v-if="field.showIf(companyAssociatedEutaxonomyNonFinancialsData.data)"
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
          <div v-if="postEutaxonomyNonFinancialsDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else data-test="failedUploadMessage" :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in eutaxonomyNonFinancialsDataModel" :key="category.name">
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
import { eutaxonomyNonFinancialsDataModel } from '@/frameworks/custom/EuTaxoNonFinancialsStaticUploadConfig';
import {
  type CompanyAssociatedDataEutaxonomyNonFinancialsData,
  type CompanyReport,
  DataTypeEnum,
  type EutaxonomyNonFinancialsData,
} from '@clients/backend';
import { type LocationQueryValue, useRoute } from 'vue-router';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationUtils';
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
import FinancialShareExtendedDataPointFormField from '@/components/forms/parts/kpiSelection/FinancialShareExtendedDataPointFormField.vue';
import AlignedActivitiesFormField from '@/components/forms/parts/kpiSelection/AlignedActivitiesFormField.vue';
import NonAlignedActivitiesFormField from '@/components/forms/parts/kpiSelection/NonAlignedActivitiesFormField.vue';
import AssuranceFormField from '@/components/forms/parts/kpiSelection/AssuranceFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import InputSwitch from 'primevue/inputswitch';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, uploadFiles } from '@/utils/FileUploadUtils';
import { type Subcategory } from '@/utils/GenericFrameworkTypes';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import YesNoNaExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoNaExtendedDataPointFormField.vue';
import DateExtendedDataPointFormField from '@/components/forms/parts/fields/DateExtendedDataPointFormField.vue';
import PercentageExtendedDataPointFormField from '@/components/forms/parts/fields/PercentageExtendedDataPointFormField.vue';
import RadioButtonsExtendedDataPointFormField from '@/components/forms/parts/fields/RadioButtonsExtendedDataPointFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateEuTaxonomyNonFinancials',
  components: {
    SubmitButton,
    SubmitSideBar,
    UploadFormHeader,
    SuccessMessage,
    FailMessage,
    FormKit,
    Card,
    PrimeButton,
    Calendar,
    InputSwitch,
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
    UploadReports,
    FinancialShareExtendedDataPointFormField,
    AlignedActivitiesFormField,
    AssuranceFormField,
    NonAlignedActivitiesFormField,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyDataPointFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
    YesNoNaExtendedDataPointFormField,
    DateExtendedDataPointFormField,
    PercentageExtendedDataPointFormField,
    RadioButtonsExtendedDataPointFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createEuTaxonomyNonFinancialsForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedEutaxonomyNonFinancialsData: {} as CompanyAssociatedDataEutaxonomyNonFinancialsData,
      eutaxonomyNonFinancialsDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postEutaxonomyNonFinancialsDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      documentsToUpload: [] as DocumentToUpload[],
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      reportingPeriod: undefined as undefined | Date,
      editMode: false,
      listOfFilledKpis: [] as Array<string>,
      templateDataId: null as LocationQueryValue | LocationQueryValue[],
      templateReportingPeriod: null as LocationQueryValue | LocationQueryValue[],
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
      return createSubcategoryVisibilityMap(
        this.eutaxonomyNonFinancialsDataModel,
        this.companyAssociatedEutaxonomyNonFinancialsData.data
      );
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  created() {
    this.templateDataId = this.route.query.templateDataId;
    this.templateReportingPeriod = this.route.query.reportingPeriod;
    if (
      (this.templateDataId && typeof this.templateDataId === 'string') ||
      (this.templateReportingPeriod && typeof this.templateReportingPeriod === 'string')
    ) {
      this.editMode = true;
      void this.loadEutaxonomyNonFinancialsData();
    } else {
      this.waitingForData = false;
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }
  },
  methods: {
    /**
     * Builds an api to get and upload EU Taxonomy non-financials data
     * @returns the api
     */
    buildEuTaxonomyNonFinancialsDataApi(): PublicFrameworkDataApi<EutaxonomyNonFinancialsData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.EutaxonomyNonFinancials);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      } else return undefined;
    },
    /**
     * Loads the EutaxonomyNonFinancials-Dataset identified either by the provided reportingPeriod and companyId,
     * or the dataId, and pre-configures the form to contain the data from the dataset
     */
    async loadEutaxonomyNonFinancialsData(): Promise<void> {
      this.waitingForData = true;
      const euTaxonomyForNonFinancialsDataControllerApi = this.buildEuTaxonomyNonFinancialsDataApi();
      if (euTaxonomyForNonFinancialsDataControllerApi) {
        let dataResponse;
        if (this.templateDataId) {
          dataResponse = await euTaxonomyForNonFinancialsDataControllerApi.getFrameworkData(
            this.templateDataId.toString()
          );
        } else if (this.templateReportingPeriod) {
          dataResponse = await euTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedDataByDimensions(
            this.templateReportingPeriod.toString(),
            this.companyID
          );
        }
        if (!dataResponse) {
          this.waitingForData = false;
          throw ReferenceError('DataResponse from EuTaxonomyNonFinancialsDataController invalid.');
        }

        const euTaxonomyNonFinancialsResponseData = dataResponse.data;
        this.listOfFilledKpis = getFilledKpis(euTaxonomyNonFinancialsResponseData);
        if (euTaxonomyNonFinancialsResponseData?.reportingPeriod) {
          this.reportingPeriod = new Date(euTaxonomyNonFinancialsResponseData.reportingPeriod);
        }
        this.referencedReportsForPrefill = euTaxonomyNonFinancialsResponseData.data.general?.referencedReports ?? {};
        this.companyAssociatedEutaxonomyNonFinancialsData = objectDropNull(euTaxonomyNonFinancialsResponseData);
        this.waitingForData = false;
      }
    },

    /**
     * Sends data to add EuTaxonomyNonFinancials data
     */
    async postEutaxonomyNonFinancialsData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documentsToUpload.length > 0) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedEutaxonomyNonFinancialsData.data as ObjectType,
            Object.keys(this.namesAndReferencesOfAllCompanyReportsForTheDataset)
          );

          await uploadFiles(this.documentsToUpload, assertDefined(this.getKeycloakPromise));
        }

        const euTaxonomyForNonFinancialsDataControllerApi = this.buildEuTaxonomyNonFinancialsDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedEutaxonomyNonFinancialsData.companyId,
          this.getKeycloakPromise
        );

        await euTaxonomyForNonFinancialsDataControllerApi!.postFrameworkData(
          this.companyAssociatedEutaxonomyNonFinancialsData,
          isCompanyOwnerOrDataUploader
        );

        this.$emit('datasetCreated');
        this.dataDate = undefined;
        this.message = 'Upload successfully executed.';
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if ((error as Error).message) {
          this.message = formatAxiosErrorMessage(error as Error);
        } else {
          this.message =
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
        this.uploadSucceded = false;
      } finally {
        this.postEutaxonomyNonFinancialsDataProcessed = true;
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
