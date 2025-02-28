<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - SFDR</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading SFDR data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedSfdrData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postSfdrData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in sfdrDataModel"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="uploadFormSection grid" v-for="subcategory in category.subcategories" :key="subcategory">
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="col-3 p-3 topicLabel">
                      <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                      <div :class="`p-badge badge-${category.color}`">
                        <span>{{ category.label.toUpperCase() }}</span>
                      </div>
                    </div>

                    <div class="col-9 formFields">
                      <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                        <component
                          v-if="field.showIf(companyAssociatedSfdrData.data)"
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
                          @reports-updated="updateDocumentsList"
                          @field-specific-documents-updated="
                            updateDocumentsOnField(`${category.name}.${subcategory.name}.${field.name}`, $event)
                          "
                          :ref="field.name"
                        />
                      </FormKit>
                    </div>
                  </template>
                </div>
              </FormKit>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postSfdrDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in sfdrDataModel" :key="category">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory">
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
import { sfdrDataModel } from '@/frameworks/sfdr/UploadConfig';
import { type CompanyAssociatedDataSfdrData, type CompanyReport, DataTypeEnum, type SfdrData } from '@clients/backend';
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
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, getAvailableFileNames, uploadFiles } from '@/utils/FileUploadUtils';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import { type Subcategory } from '@/utils/GenericFrameworkTypes';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import HighImpactClimateSectorsFormField from '@/components/forms/parts/fields/HighImpactClimateSectorsFormField.vue';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import { HighImpactClimateSectorsNaceCodes } from '@/types/HighImpactClimateSectors';
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

const referenceableReportsFieldId = 'referenceableReports';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateSfdrDataset',
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
      formId: 'createSFDRForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedSfdrData: {} as CompanyAssociatedDataSfdrData,
      sfdrDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postSfdrDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      climateSectorsForPrefill: [] as Array<string>,
      listOfFilledKpis: [] as Array<string>,
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      fieldSpecificDocuments: new Map<string, DocumentToUpload[]>(),
      templateDataId: null as LocationQueryValue | LocationQueryValue[],
      templateReportingPeriod: null as LocationQueryValue | LocationQueryValue[],
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedSfdrData.data?.general?.general?.fiscalYearEnd;
        if (currentDate === undefined) {
          return '';
        } else {
          const currentDateSegments = currentDate.split('-');
          return currentDateSegments[0] ?? new Date().getFullYear();
        }
      },
      set() {
        // IGNORED
      },
    },
    namesOfAllCompanyReportsForTheDataset(): string[] {
      return getAvailableFileNames(this.namesAndReferencesOfAllCompanyReportsForTheDataset);
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.sfdrDataModel, this.companyAssociatedSfdrData.data);
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
      void this.loadSfdrData();
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Builds an api to get and upload Sfdr data
     * @returns the api
     */
    buildSfdrDataApi(): PublicFrameworkDataApi<SfdrData> {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.Sfdr);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      }
    },

    /**
     * Loads the SFDR-Dataset identified either by the provided reportingPeriod and companyId,
     * or the dataId, and pre-configures the form to contain the data from the dataset
     */
    async loadSfdrData(): Promise<void> {
      this.waitingForData = true;
      const sfdrDataControllerApi = this.buildSfdrDataApi();
      if (sfdrDataControllerApi) {
        let dataResponse;
        if (this.templateDataId) {
          dataResponse = await sfdrDataControllerApi.getFrameworkData(this.templateDataId.toString());
        } else if (this.templateReportingPeriod) {
          dataResponse = await sfdrDataControllerApi.getCompanyAssociatedDataByDimensions(
            this.templateReportingPeriod.toString(),
            this.companyID
          );
        }
        if (!dataResponse) {
          this.waitingForData = false;
          throw ReferenceError('DataResponse from SfdrDataController invalid.');
        }
        const sfdrResponseData = dataResponse.data;
        this.listOfFilledKpis = getFilledKpis(sfdrResponseData.data);
        this.referencedReportsForPrefill = sfdrResponseData.data.general.general.referencedReports ?? {};
        this.climateSectorsForPrefill = sfdrResponseData?.data?.environmental?.energyPerformance
          ?.applicableHighImpactClimateSectors
          ? Object.keys(
              sfdrResponseData?.data?.environmental?.energyPerformance?.applicableHighImpactClimateSectors
            ).map((it): string => {
              return HighImpactClimateSectorsNaceCodes[it as keyof typeof HighImpactClimateSectorsNaceCodes] ?? it;
            })
          : [];
        this.companyAssociatedSfdrData = objectDropNull(
          sfdrResponseData as ObjectType
        ) as CompanyAssociatedDataSfdrData;

        this.waitingForData = false;
      }
    },
    /**
     * Sends data to add SFDR data
     */
    async postSfdrData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.get(referenceableReportsFieldId)?.length) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedSfdrData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset
          );
        }
        const documentsToUpload = Array.from(this.fieldSpecificDocuments.values()).flat();
        await uploadFiles(documentsToUpload, assertDefined(this.getKeycloakPromise));

        const sfdrDataControllerApi = this.buildSfdrDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedSfdrData.companyId,
          this.getKeycloakPromise
        );

        await sfdrDataControllerApi.postFrameworkData(this.companyAssociatedSfdrData, isCompanyOwnerOrDataUploader);

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
        this.postSfdrDataProcessed = true;
      }
    },
    /**
     * updates the list of documents that were uploaded
     * @param reportsNamesAndReferences repots names and references
     * @param reportsToUpload reports to upload
     */
    updateDocumentsList(reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]) {
      this.namesAndReferencesOfAllCompanyReportsForTheDataset = reportsNamesAndReferences;
      if (reportsToUpload.length) {
        this.fieldSpecificDocuments.set(referenceableReportsFieldId, reportsToUpload);
      } else {
        this.fieldSpecificDocuments.delete(referenceableReportsFieldId);
      }
    },
    /**
     * Updates the referenced document for a specific field
     * @param fieldId an identifier for the field
     * @param referencedDocument the documen that is referenced
     */
    updateDocumentsOnField(fieldId: string, referencedDocument: DocumentToUpload | undefined) {
      if (referencedDocument) {
        this.fieldSpecificDocuments.set(fieldId, [referencedDocument]);
      } else {
        this.fieldSpecificDocuments.delete(fieldId);
      }
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
      climateSectorsForPrefill: computed(() => {
        return this.climateSectorsForPrefill;
      }),
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
