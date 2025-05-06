<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - Additional Company Information</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading Additional Company Information data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedAdditionalCompanyInformationData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postAdditionalCompanyInformationData"
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
                  :description="'The year for which the data is reported.'"
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
              <FormKit
                type="group"
                v-for="category in additionalCompanyInformationDataModel"
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
                          v-if="field.showIf(companyAssociatedAdditionalCompanyInformationData.data as FrameworkData)"
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
          <div v-if="postAdditionalCompanyInformationDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in additionalCompanyInformationDataModel" :key="category.name">
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
import { computed, defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import Tooltip from 'primevue/tooltip';
import PrimeButton from 'primevue/button';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import Calendar from 'primevue/calendar';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { additionalCompanyInformationDataModel } from '@/frameworks/additional-company-information/UploadConfig';
import {
  type AdditionalCompanyInformationData,
  type CompanyAssociatedDataAdditionalCompanyInformationData,
  type CompanyReport,
  DataTypeEnum,
} from '@clients/backend';
import { type LocationQueryValue, useRoute } from 'vue-router';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationUtils';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, getAvailableFileNames, uploadFiles } from '@/utils/FileUploadUtils';
import { type FrameworkData, type Subcategory } from '@/utils/GenericFrameworkTypes';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import BaseDataPointFormField from '@/components/forms/parts/elements/basic/BaseDataPointFormField.vue';
import DateExtendedDataPointFormField from '@/components/forms/parts/fields/DateExtendedDataPointFormField.vue';
import RadioButtonsExtendedDataPointFormField from '@/components/forms/parts/fields/RadioButtonsExtendedDataPointFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';

const referenceableReportsFieldId = 'referenceableReports';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateAdditionalCompanyInformationDataset',
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
    DateFormField,
    UploadReports,
    DateExtendedDataPointFormField,
    RadioButtonsExtendedDataPointFormField,
    CurrencyDataPointFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createAdditionalCompanyInformationForm',
      waitingForData: true,
      companyAssociatedAdditionalCompanyInformationData: {} as CompanyAssociatedDataAdditionalCompanyInformationData,
      additionalCompanyInformationDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postAdditionalCompanyInformationDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      reportingPeriod: undefined as undefined | Date,
      listOfFilledKpis: [] as Array<string>,
      fieldSpecificDocuments: new Map<string, DocumentToUpload[]>(),
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
    namesOfAllCompanyReportsForTheDataset(): string[] {
      return getAvailableFileNames(this.namesAndReferencesOfAllCompanyReportsForTheDataset);
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(
        this.additionalCompanyInformationDataModel,
        this.companyAssociatedAdditionalCompanyInformationData.data as FrameworkData
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
      void this.loadAdditionalCompanyInformationData();
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Builds an api to get and upload Additional Company Information data
     * @returns the api
     */
    buildAdditionalCompanyInformationDataApi(): PublicFrameworkDataApi<AdditionalCompanyInformationData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.AdditionalCompanyInformation);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      }
      return undefined;
    },

    /**
     * Loads the AdditionalCompanyInformation-Dataset identified either by the provided reportingPeriod and companyId,
     * or the dataId, and pre-configures the form to contain the data from the dataset
     */
    async loadAdditionalCompanyInformationData(): Promise<void> {
      this.waitingForData = true;
      const additionalCompanyInformationDataControllerApi = this.buildAdditionalCompanyInformationDataApi();
      if (additionalCompanyInformationDataControllerApi) {
        let dataResponse;
        if (this.templateDataId) {
          dataResponse = await additionalCompanyInformationDataControllerApi.getFrameworkData(
            this.templateDataId.toString()
          );
        } else if (this.templateReportingPeriod) {
          dataResponse = await additionalCompanyInformationDataControllerApi.getCompanyAssociatedDataByDimensions(
            this.templateReportingPeriod.toString(),
            this.companyID
          );
        }
        if (!dataResponse) {
          this.waitingForData = false;
          throw ReferenceError('DataResponse from AdditionalCompanyInformationDataController invalid.');
        }

        const additionalCompanyInformationResponseData = dataResponse.data;
        this.listOfFilledKpis = getFilledKpis(additionalCompanyInformationResponseData.data);
        this.referencedReportsForPrefill =
          additionalCompanyInformationResponseData.data?.general?.general?.referencedReports ?? {};
        this.companyAssociatedAdditionalCompanyInformationData = objectDropNull(
          additionalCompanyInformationResponseData
        ) as CompanyAssociatedDataAdditionalCompanyInformationData;
        this.waitingForData = false;
      }
    },
    /**
     * Sends data to add AdditionalCompanyInformation data
     */
    async postAdditionalCompanyInformationData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.get(referenceableReportsFieldId)?.length) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedAdditionalCompanyInformationData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset
          );
        }
        const documentsToUpload = Array.from(this.fieldSpecificDocuments.values()).flat();
        await uploadFiles(documentsToUpload, assertDefined(this.getKeycloakPromise));

        const additionalCompanyInformationDataControllerApi = this.buildAdditionalCompanyInformationDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedAdditionalCompanyInformationData.companyId,
          this.getKeycloakPromise
        );

        await additionalCompanyInformationDataControllerApi?.postFrameworkData(
          this.companyAssociatedAdditionalCompanyInformationData,
          isCompanyOwnerOrDataUploader
        );

        this.$emit('datasetCreated');
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
        this.postAdditionalCompanyInformationDataProcessed = true;
      }
    },
    /**
     * updates the list of documents that were uploaded
     * @param reportsNamesAndReferences reports names and references
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
     * @param referencedDocument the document that is referenced
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
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
