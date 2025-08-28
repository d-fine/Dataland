<template>
  <div class="container">
    <h2>New Dataset - PCAF</h2>
    <Divider />
    <div v-if="waitingForData">
      <h2>Loading PCAF data...</h2>
      <DatalandProgressSpinner />
    </div>
    <FormKit
      v-else
      v-model="companyAssociatedDataPcafData"
      :actions="false"
      type="form"
      :id="formId"
      :name="formId"
      @submit="postPcafData"
      @submit-invalid="checkCustomInputs"
      form-class="uploadFormWrapper form-container"
    >
      <div class="form-content">
        <FormKit type="hidden" name="companyId" :model-value="companyID" />
        <FormKit type="hidden" name="reportingPeriod" :model-value="reportingPeriod?.getFullYear().toString()" />

        <div class="subcategory-container">
          <div class="label-container">
            <h4 class="subcategory-label">Reporting Period</h4>
          </div>
          <div class="form-field-container">
            <UploadFormHeader label="Reporting Period" description="The year for which the data is reported." />
            <DatePicker
              input-id="reporting-period-picker"
              v-model="reportingPeriod"
              :showIcon="true"
              view="year"
              dateFormat="yy"
              validation="required"
            />
          </div>
        </div>

        <FormKit type="group" name="data" label="data">
          <FormKit type="group" v-for="category in pcafDataModel" :key="category.name" :name="category.name">
            <div v-for="subcategory in category.subcategories" :key="subcategory.name">
              <div v-if="subcategoryVisibilityMap.get(subcategory) ?? true">
                <div class="subcategory-container">
                  <div class="label-container">
                    <h4 :id="subcategory.name" class="subcategory-label">{{ subcategory.label }}</h4>
                    <Tag :value="category.label.toUpperCase()" severity="secondary" />
                  </div>
                  <div class="form-field-container">
                    <FormKit
                      type="group"
                      v-for="field in subcategory.fields"
                      :key="field.name"
                      :name="subcategory.name"
                    >
                      <component
                        v-if="field.showIf(companyAssociatedDataPcafData?.data as PcafData)"
                        :is="getComponentByName(field.component)"
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
                      />
                    </FormKit>
                  </div>
                </div>
              </div>
            </div>
          </FormKit>
        </FormKit>
      </div>

      <div class="sidebar">
        <PrimeButton type="submit" label="SUBMIT DATA" :disabled="isJustClicked" @click="onSubmitButtonClick" fluid />
        <div v-if="isPostRequestProcessed" class="message-container">
          <Message v-if="!errorMessage" severity="success">Upload successfully executed.</Message>
          <Message v-else severity="error">{{ errorMessage }}</Message>
        </div>

        <h4>On this page</h4>
        <ul>
          <li v-for="category in pcafDataModel" :key="category.name">
            <ul>
              <li v-for="subcategory in category.subcategories" :key="subcategory.name">
                <a v-if="subcategoryVisibilityMap.get(subcategory) ?? true" :href="`#${subcategory.name}`">
                  {{ subcategory.label }}
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </FormKit>
  </div>
</template>

<script setup lang="ts">
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { getComponentByName } from '@/components/forms/UploadPageComponentDictionary.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { pcafDataModel } from '@/frameworks/pcaf/UploadConfig.ts';
import { ApiClientProvider } from '@/services/ApiClients';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter.ts';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils.ts';
import { getFilledKpis } from '@/utils/DataPoint.ts';
import type { Subcategory } from '@/utils/GenericFrameworkTypes.ts';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { objectDropNull } from '@/utils/UpdateObjectUtils.ts';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils.ts';
import { checkCustomInputs } from '@/utils/ValidationUtils.ts';
import { type CompanyAssociatedDataPcafData, DataTypeEnum, type PcafData } from '@clients/backend';
import { submitForm } from '@formkit/core';
import { FormKit } from '@formkit/vue';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import DatePicker from 'primevue/datepicker';
import Divider from 'primevue/divider';
import Message from 'primevue/message';
import Tag from 'primevue/tag';
import { computed, inject, onMounted, provide, ref } from 'vue';
import { type LocationQueryValue, useRoute } from 'vue-router';
import { type DocumentMetaInfoResponse } from '@clients/documentmanager';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const props = defineProps({
  companyID: {
    type: String,
    required: true,
  },
});
const emits = defineEmits(['datasetCreated']);
const route = useRoute();

const companyAssociatedDataPcafData = ref<CompanyAssociatedDataPcafData>({} as CompanyAssociatedDataPcafData);
const errorMessage = ref('');
const formId = 'createPcafForm';
const isJustClicked = ref(false);
const isPostRequestProcessed = ref(false);
const listOfFilledKpis = ref<string[]>();
const reportingPeriod = ref<Date | undefined>(undefined);
const templateDataId: LocationQueryValue | LocationQueryValue[] = route.query.templateDataId;
const templateReportingPeriod: LocationQueryValue | LocationQueryValue[] = route.query.reportingPeriod;
const waitingForData = ref(false);

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.Pcaf);
const pcafDataApi: PublicFrameworkDataApi<PcafData> | undefined = frameworkDefinition?.getPublicFrameworkApiClient(
  undefined,
  apiClientProvider.axiosInstance
);

const subcategoryVisibilityMap = computed((): Map<Subcategory, boolean> => {
  if (companyAssociatedDataPcafData.value) {
    return createSubcategoryVisibilityMap(pcafDataModel, companyAssociatedDataPcafData.value?.data);
  }
  return new Map<Subcategory, boolean>();
});
const namesAndReferencesOfAllCompanyReportsForTheDataset = ref<Record<string, string>>({});

onMounted(() => {
  if (
    (templateDataId && typeof templateDataId === 'string') ||
    (templateReportingPeriod && typeof templateReportingPeriod === 'string')
  ) {
    void loadPcafData();
  }
  void updateDocumentsList();
});

const providedReports = computed(() => namesAndReferencesOfAllCompanyReportsForTheDataset.value);
provide('namesAndReferencesOfAllCompanyReportsForTheDataset', providedReports);

/**
 * Loads the PCAF dataset identified either by the provided reportingPeriod and companyId,
 * or the dataId, and pre-configures the form to contain the data from the dataset
 */
async function loadPcafData(): Promise<void> {
  waitingForData.value = true;
  let pcafData;
  try {
    if (templateDataId) {
      pcafData = (await pcafDataApi!.getFrameworkData(templateDataId.toString())).data;
    } else if (templateReportingPeriod) {
      pcafData = (
        await pcafDataApi!.getCompanyAssociatedDataByDimensions(templateReportingPeriod.toString(), props.companyID)
      )?.data;
    }
    if (!pcafData) {
      throw ReferenceError('Response from PcafDataController invalid.');
    }
    listOfFilledKpis.value = getFilledKpis(pcafData);
    companyAssociatedDataPcafData.value = objectDropNull(pcafData) as CompanyAssociatedDataPcafData;
  } catch (e) {
    console.error('Error while loading PCAF data', e);
  } finally {
    waitingForData.value = false;
  }
}

/**
 * Send POST request to add PCAF data
 */
async function postPcafData(): Promise<void> {
  try {
    const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
      companyAssociatedDataPcafData.value.companyId,
      getKeycloakPromise
    );

    await pcafDataApi?.postFrameworkData(companyAssociatedDataPcafData.value, isCompanyOwnerOrDataUploader);
    emits('datasetCreated');
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = formatAxiosErrorMessage(error);
    } else {
      errorMessage.value =
        'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
    }
    console.error(error);
  } finally {
    isPostRequestProcessed.value = true;
  }
}

/**
 * Triggers the form submit, and disables the submitButton for a short amount of time.
 */
function onSubmitButtonClick(): void {
  if (isJustClicked.value) {
    return;
  }
  submitForm(formId);
  isJustClicked.value = true;
  setTimeout(() => (isJustClicked.value = false), 500);
}

/**
 * Updates the list of available document metadata based on uploaded documents.
 * @returns Object with document names as keys and document IDs as values
 */
async function updateDocumentsList(): Promise<Record<string, string>> {
  try {
    const documentControllerApi = apiClientProvider.apiClients.documentController;
    const documentsObject: Record<string, string> = {};

    try {
      const response = await documentControllerApi.searchForDocumentMetaInformation(
        companyAssociatedDataPcafData.value.companyId
      );
      const documents = response.data;

      documents.forEach((doc: DocumentMetaInfoResponse) => {
        if (doc.documentName && doc.documentId) {
          documentsObject[doc.documentName] = doc.documentId;
        }
      });

      namesAndReferencesOfAllCompanyReportsForTheDataset.value = documentsObject;

      return documentsObject;
    } catch (error) {
      console.error(`Error fetching documents:`, error);
      return {};
    }
  } catch (error) {
    console.error('Error updating documents list:', error);
    return {};
  }
}

/**
 * Updates the documents associated with a specific field in the PCAF data.
 * @param fieldName - The name of the field to update documents for.
 * @param documents - The list of document IDs to associate with the field.
 */
function updateDocumentsOnField(fieldName: string, documentIds: string[]): void {
  const data = companyAssociatedDataPcafData.value as CompanyAssociatedDataPcafData & {
    fieldSpecificDocuments?: Record<string, string[]>;
  };
  if (!data.fieldSpecificDocuments) {
    data.fieldSpecificDocuments = {};
  }
  data.fieldSpecificDocuments[fieldName] = documentIds;
}
</script>

<style scoped>
.container {
  background-color: var(--p-surface-50);
  padding: var(--spacing-xl) var(--spacing-xxl);
  text-align: start;
}

.form-container {
  display: flex;
  gap: var(--spacing-xl);

  .form-content {
    flex-grow: 5;

    .subcategory-container {
      margin-bottom: var(--spacing-xl);
      display: flex;

      .label-container {
        flex-basis: 25%;

        h4 {
          margin-bottom: var(--spacing-xxs);
        }
      }

      .form-field-container {
        flex: auto;
        padding: var(--spacing-md);
        background-color: var(--p-surface-0);
      }
    }
  }

  .sidebar {
    margin-left: auto;
    text-align: start;
    flex-grow: 1;

    .message-container {
      margin: var(--spacing-sm) 0;
    }

    ul {
      margin: 0;
      padding: 0;

      li {
        list-style: none;
        margin: 0.5rem 0;

        a {
          color: var(--p-surface-500);
          text-decoration: none;

          &:hover {
            color: var(--p-primary-color);
            cursor: pointer;
          }
        }
      }
    }
  }
}
</style>
