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
                :key="category.name"
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
                      <FormKit
                        v-for="field in subcategory.fields"
                        :key="field.name"
                        type="group"
                        :name="subcategory.name"
                      >
                        <component
                          v-if="field.showIf(companyAssociatedVsmeData.data)"
                          :is="getComponentByName(field.component)"
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
<script setup lang="ts">
import { FormKit } from '@formkit/vue';
import { computed, inject, provide, ref } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationUtils';

import { smoothScroll } from '@/utils/SmoothScroll';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import Calendar from 'primevue/calendar';
import type Keycloak from 'keycloak-js';
import { type Category } from '@/utils/GenericFrameworkTypes';
import { AxiosError } from 'axios';
import { type CompanyAssociatedDataVsmeData, DataTypeEnum, type VsmeData } from '@clients/backend';
import { vsmeDataModel } from '@/frameworks/vsme/UploadConfig';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';

import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';

import { type DocumentToUpload, getAvailableFileNames } from '@/utils/FileUploadUtils';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import { getBasePrivateFrameworkDefinition } from '@/frameworks/BasePrivateFrameworkRegistry';
import { type PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';

import { getComponentByName } from '@/components/forms/UploadPageComponentDictionary.ts';
const referenceableReportsFieldId = 'referenceableReports';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const emit = defineEmits(['datasetCreated']);
defineProps<{
  companyID: string;
}>();

const formId = 'createVsmeForm';
const companyAssociatedVsmeData = ref({} as CompanyAssociatedDataVsmeData);
const message = ref('');
const uploadSucceded = ref(false);
const postVsmeDataProcessed = ref(false);
const messageCounter = ref(0);
const namesAndReferencesOfAllCompanyReportsForTheDataset = ref({});
const reportingPeriod = ref<Date | undefined>(undefined);
const fieldSpecificDocuments = ref(new Map<string, DocumentToUpload[]>());

const reportingPeriodYear = computed(() => {
  if (reportingPeriod.value) {
    return reportingPeriod.value.getFullYear().toString();
  }
  return undefined;
});

const visibleCategories = computed(() => {
  return vsmeDataModel.filter((category: Category) => category.showIf(companyAssociatedVsmeData.value.data));
});

const subcategoryVisibilityMap = computed(() => {
  return createSubcategoryVisibilityMap(vsmeDataModel, companyAssociatedVsmeData.value.data);
});

const namesOfAllCompanyReportsForTheDataset = computed(() => {
  return getAvailableFileNames(namesAndReferencesOfAllCompanyReportsForTheDataset.value);
});

provide('namesAndReferencesOfAllCompanyReportsForTheDataset', namesAndReferencesOfAllCompanyReportsForTheDataset);

/**
 * Builds an api to upload Vsme data
 * @returns the api
 */
const buildVsmeDataApi = (): PrivateFrameworkDataApi<VsmeData> | undefined => {
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  const frameworkDefinition = getBasePrivateFrameworkDefinition(DataTypeEnum.Vsme);
  if (frameworkDefinition) {
    return frameworkDefinition.getPrivateFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
  } else return undefined;
};

/**
 * Sends data to add VSME data
 */
const postVsmeData = async (): Promise<void> => {
  messageCounter.value++;
  try {
    if (fieldSpecificDocuments.value.get(referenceableReportsFieldId)?.length) {
      checkIfAllUploadedReportsAreReferencedInDataModel(
        companyAssociatedVsmeData.value.data as ObjectType,
        namesOfAllCompanyReportsForTheDataset.value
      );
    }
    const documentsToUpload = Array.from(fieldSpecificDocuments.value.values()).flat();
    const files: File[] = documentsToUpload.map((documentsToUpload) => documentsToUpload.file);
    const vsmeDataControllerApi = buildVsmeDataApi();
    await vsmeDataControllerApi!.postFrameworkData(companyAssociatedVsmeData.value, files);
    emit('datasetCreated');
    message.value = 'Upload successfully executed.';
    uploadSucceded.value = true;
  } catch (error) {
    console.error(error);
    if (error instanceof AxiosError) {
      message.value = 'An error occurred: ' + error.message;
    } else if ((error as Error).message) {
      message.value = formatAxiosErrorMessage(error as Error);
    }
    uploadSucceded.value = false;
  } finally {
    postVsmeDataProcessed.value = true;
  }
};

/**
 * updates the list of documents that were uploaded
 * @param reportsNamesAndReferences reports names and references
 * @param reportsToUpload reports to upload
 */
const updateDocumentsList = (reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]): void => {
  namesAndReferencesOfAllCompanyReportsForTheDataset.value = reportsNamesAndReferences;
  if (reportsToUpload.length) {
    fieldSpecificDocuments.value.set(referenceableReportsFieldId, reportsToUpload);
  } else {
    fieldSpecificDocuments.value.delete(referenceableReportsFieldId);
  }
};

/**
 * Updates the referenced document for a specific field
 * @param fieldId an identifier for the field
 * @param referencedDocument the document that is referenced
 */
const updateDocumentsOnField = (fieldId: string, referencedDocument: DocumentToUpload | undefined): void => {
  if (referencedDocument) {
    fieldSpecificDocuments.value.set(fieldId, [referencedDocument]);
  } else {
    fieldSpecificDocuments.value.delete(fieldId);
  }
};
</script>
