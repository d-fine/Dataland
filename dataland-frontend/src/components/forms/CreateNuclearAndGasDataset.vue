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
            @submit="postNuclearAndGasData"
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
                v-for="category in nuclearAndGasDataModel"
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
                          v-if="field.showIf(companyAssociatedNuclearAndGasData.data as NuclearAndGasData)"
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
import { nuclearAndGasDataModel } from '@/frameworks/nuclear-and-gas/UploadConfig';
import {
  type CompanyAssociatedDataNuclearAndGasData,
  type CompanyReport,
  DataTypeEnum,
  type NuclearAndGasData,
} from '@clients/backend';
import { useRoute } from 'vue-router';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationUtils';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import type { FrameworkData, Subcategory } from '@/utils/GenericFrameworkTypes';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import BaseDataPointFormField from '@/components/forms/parts/elements/basic/BaseDataPointFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';
import { type DocumentToUpload, getAvailableFileNames, uploadFiles } from '@/utils/FileUploadUtils';
import NuclearAndGasFormElement from '@/components/forms/parts/elements/derived/NuclearAndGasFormElement.vue';

const referenceableReportsFieldId = 'referenceableReports';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateNuclearAndGasDataset',
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
    NuclearAndGasFormElement,
    UploadReports,
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
      companyAssociatedNuclearAndGasData: {} as CompanyAssociatedDataNuclearAndGasData,
      nuclearAndGasDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postNuclearAndGasDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      reportingPeriod: undefined as undefined | Date,
      listOfFilledKpis: [] as Array<string>,
      fieldSpecificDocuments: new Map<string, DocumentToUpload[]>(),
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
        this.nuclearAndGasDataModel,
        this.companyAssociatedNuclearAndGasData.data as FrameworkData
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
    const reportingPeriod = this.route.query.reportingPeriod;
    if (reportingPeriod && typeof reportingPeriod === 'string') {
      void this.loadNuclearAndGasData(reportingPeriod, this.companyID);
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
    buildNuclearAndGasDataApi(): PublicFrameworkDataApi<NuclearAndGasData> | null {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.NuclearAndGas);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      }
      return null;
    },

    /**
     * Loads the Nuclear-and-Gas-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param reportingPeriod the relevant reporting period
     * @param companyId the company id
     */
    async loadNuclearAndGasData(reportingPeriod: string, companyId: string): Promise<void> {
      this.waitingForData = true;
      const nuclearAndGasDataControllerApi = this.buildNuclearAndGasDataApi();
      if (nuclearAndGasDataControllerApi) {
        const dataResponse = await nuclearAndGasDataControllerApi.getCompanyAssociatedDataByDimensions(companyId, reportingPeriod);
        const nuclearAndGasResponseData = dataResponse.data;
        this.listOfFilledKpis = getFilledKpis(nuclearAndGasResponseData.data);
        this.referencedReportsForPrefill = nuclearAndGasResponseData.data?.general?.general?.referencedReports ?? {};
        this.companyAssociatedNuclearAndGasData = objectDropNull(
          nuclearAndGasResponseData
        ) as CompanyAssociatedDataNuclearAndGasData;
        this.waitingForData = false;
      }
    },
    /**
     * Sends data to add NuclearAndGas data
     */
    async postNuclearAndGasData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.get(referenceableReportsFieldId)?.length) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedNuclearAndGasData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset
          );
        }
        const documentsToUpload = Array.from(this.fieldSpecificDocuments.values()).flat();
        await uploadFiles(documentsToUpload, assertDefined(this.getKeycloakPromise));

        const nuclearAndGasDataControllerApi = this.buildNuclearAndGasDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedNuclearAndGasData.companyId,
          this.getKeycloakPromise
        );

        await nuclearAndGasDataControllerApi?.postFrameworkData(
          this.companyAssociatedNuclearAndGasData,
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
        this.postNuclearAndGasDataProcessed = true;
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
