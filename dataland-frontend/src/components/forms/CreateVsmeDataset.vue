<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - VSME</template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedVsmeData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postVsmeData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" :modelValue="reportingPeriodYear" name="reportingPeriod" />
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
                  <FormKit
                    validation="required"
                    name="reportingPeriod"
                    validation-label="Reporting Period"
                    v-model="reportingPeriodYear"
                    outer-class="hidden-input"
                  />
                </div>
              </div>
            </div>
            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in visibleCategories"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div
                  class="uploadFormSection grid"
                  v-for="subcategory in category.subcategories"
                  :key="subcategory.name"
                >
                  <template v-if="subcategoryVisibilityMap.get(subcategory) ?? true">
                    <div class="col-3 p-3 topicLabel">
                      <h4 :id="`${category.name}-${subcategory.name}`" class="anchor title">{{ subcategory.label }}</h4>
                      <div :class="`p-badge badge-${category.color}`">
                        <span>{{ category.label.toUpperCase() }}</span>
                      </div>
                    </div>

                    <div class="col-9 formFields">
                      <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                        <component
                          v-if="field.showIf(companyAssociatedVsmeData.data)"
                          :is="field.component"
                          :label="field.label"
                          :placeholder="field.placeholder"
                          :description="field.description"
                          :name="field.name"
                          :options="field.options"
                          :required="field.required"
                          :validation="field.validation"
                          :unit="field.unit"
                          :validation-label="field.validationLabel"
                          :data-test="field.name"
                          :ref="field.name"
                          @reportsUpdated="updateDocumentsList"
                          @field-specific-documents-updated="
                            updateDocumentsOnField(`${category.name}.${subcategory.name}.${field.name}`, $event)
                          "
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
          <div v-if="postVsmeDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in visibleCategories" :key="category.name">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory.name">
                  <a
                    v-if="subcategoryVisibilityMap.get(subcategory) ?? true"
                    @click="smoothScroll(`#${category.name}-${subcategory.name}`)"
                    >{{ category.label + ': ' + subcategory.label }}</a
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
import { computed, defineComponent, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationsUtils';
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import { smoothScroll } from '@/utils/SmoothScroll';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import Calendar from 'primevue/calendar';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { type Category, type Subcategory } from '@/utils/GenericFrameworkTypes';
import { AxiosError } from 'axios';
import { type CompanyAssociatedDataVsmeData, DataTypeEnum, type VsmeData } from '@clients/backend';
import { vsmeDataModel } from '@/frameworks/vsme/UploadConfig';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import { type DocumentToUpload, getFileName } from '@/utils/FileUploadUtils';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import { getBasePrivateFrameworkDefinition } from '@/frameworks/BasePrivateFrameworkRegistry';
import { type PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import PollutionEmissionFormField from '@/components/forms/parts/fields/PollutionEmissionFormField.vue';
import SubsidiaryFormField from '@/components/forms/parts/fields/SubsidiaryFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';
import WasteClassificationFormField from '@/components/forms/parts/fields/WasteClassificationFormField.vue';
import SiteAndAreaFormField from '@/components/forms/parts/fields/SiteAndAreaFormField.vue';
import EmployeesPerCountryFormField from '@/components/forms/parts/fields/EmployeesPerCountryFormField.vue';
import ListOfBaseDataPointsFormField from '@/components/forms/parts/fields/ListOfBaseDataPointsFormField.vue';
const referenceableReportsFieldId = 'referenceableReports';
export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateVsmeDataset',
  components: {
    FormKit,
    UploadFormHeader,
    MultiSelectFormField,
    NumberFormField,
    Card,
    PrimeButton,
    Calendar,
    SuccessMessage,
    FailMessage,
    SubmitButton,
    SubmitSideBar,
    DateFormField,
    SingleSelectFormField,
    BigDecimalExtendedDataPointFormField,
    YesNoFormField,
    NaceCodeFormField,
    UploadReports,
    PollutionEmissionFormField,
    SubsidiaryFormField,
    FreeTextFormField,
    YesNoBaseDataPointFormField,
    RadioButtonsFormField,
    WasteClassificationFormField,
    SiteAndAreaFormField,
    EmployeesPerCountryFormField,
    ListOfBaseDataPointsFormField,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createVsmeForm',
      companyAssociatedVsmeData: {} as CompanyAssociatedDataVsmeData,
      vsmeUploadConfig: vsmeDataModel,
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postVsmeDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      reportingPeriod: undefined as undefined | Date,
      fieldSpecificDocuments: new Map<string, DocumentToUpload[]>(),
    };
  },
  computed: {
    reportingPeriodYear(): string | undefined {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear().toString();
      }
      return undefined;
    },
    visibleCategories(): Category[] {
      return this.vsmeUploadConfig.filter((category: Category) => category.showIf(this.companyAssociatedVsmeData.data));
    },
    subcategoryVisibilityMap(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.vsmeUploadConfig, this.companyAssociatedVsmeData.data);
    },
    namesOfAllCompanyReportsForTheDataset(): string[] {
      return getFileName(this.namesAndReferencesOfAllCompanyReportsForTheDataset);
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  methods: {
    /**
     * Builds an api to upload Vsme data
     * @returns the api
     */
    buildVsmeDataApi(): PrivateFrameworkDataApi<VsmeData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePrivateFrameworkDefinition(DataTypeEnum.Vsme);
      if (frameworkDefinition) {
        return frameworkDefinition.getPrivateFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      } else return undefined;
    },

    /**
     * Sends data to add VSME data
     */
    async postVsmeData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.get(referenceableReportsFieldId)?.length) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedVsmeData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset
          );
        }
        const documentsToUpload = Array.from(this.fieldSpecificDocuments.values()).flat();
        const files: File[] = documentsToUpload.map((documentsToUpload) => documentsToUpload.file);
        const vsmeDataControllerApi = this.buildVsmeDataApi();
        await vsmeDataControllerApi!.postFrameworkData(this.companyAssociatedVsmeData, files);
        this.$emit('datasetCreated');
        this.message = 'Upload successfully executed.';
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = 'An error occurred: ' + error.message;
        } else if ((error as Error).message) {
          this.message = formatAxiosErrorMessage(error as Error);
        }
        this.uploadSucceded = false;
      } finally {
        this.postVsmeDataProcessed = true;
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
    };
  },
});
</script>
