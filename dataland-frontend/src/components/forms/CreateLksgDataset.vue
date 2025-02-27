<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - LkSG</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading LkSG data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
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
            <FormKit type="hidden" name="companyId" :model-value="props.companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in lksgDataModel"
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
                          v-if="field.showIf(companyAssociatedLksgData.data)"
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
                          :shouldDisableCheckboxes="true"
                          @field-specific-documents-updated="
                            updateDocumentList(`${category.name}.${subcategory.name}.${field.name}`, $event)
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
          <div v-if="postLkSGDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in lksgDataModel" :key="category.name">
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
<script setup lang="ts">
import { FormKit } from '@formkit/vue';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import { computed, inject, onMounted, provide, ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';

import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { lksgDataModel } from '@/frameworks/lksg/UploadConfig';
import { type CompanyAssociatedDataLksgData, DataTypeEnum, type LksgData } from '@clients/backend';
import { type LocationQueryValue, useRoute } from 'vue-router';
import { checkCustomInputs } from '@/utils/ValidationUtils';

import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';

import { objectDropNull } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, uploadFiles } from '@/utils/FileUploadUtils';

import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';

import { getFilledKpis } from '@/utils/DataPoint';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';
import { getComponentByName } from '@/components/forms/UploadPageComponentDictionary.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const route = useRoute();
const emit = defineEmits(['datasetCreated']);
const props = defineProps<{
  companyID: string;
}>();

const formId = 'createLkSGForm';
const waitingForData = ref(true);
const dataDate = ref<Date | undefined>(undefined);
const companyAssociatedLksgData = ref<CompanyAssociatedDataLksgData>({} as CompanyAssociatedDataLksgData);
const message = ref('');
const uploadSucceded = ref(false);
const postLkSGDataProcessed = ref(false);
const messageCounter = ref(0);
const fieldSpecificDocuments = ref(new Map<string, DocumentToUpload>());
const listOfFilledKpis = ref([] as Array<string>);
const templateDataId: LocationQueryValue | LocationQueryValue[] = route.query.templateDataId;
const templateReportingPeriod: LocationQueryValue | LocationQueryValue[] = route.query.reportingPeriod;

const yearOfDataDate = computed({
  get(): string {
    const currentDate = companyAssociatedLksgData.value.data?.general?.masterData?.dataDate;
    if (currentDate === undefined) {
      return '';
    } else {
      return currentDate.split('-')[0];
    }
  },
  set() {
    // IGNORED
  },
});

const subcategoryVisibility = computed(() => {
  return createSubcategoryVisibilityMap(lksgDataModel, companyAssociatedLksgData.value.data);
});

/**
 * Builds an api to get and upload Lksg data
 * @returns the api
 */
const buildLksgDataApi = (): PublicFrameworkDataApi<LksgData> | undefined => {
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  const frameworkDataApi = getFrameworkDataApiForIdentifier(
    DataTypeEnum.Lksg,
    apiClientProvider
  ) as PublicFrameworkDataApi<LksgData>;
  if (frameworkDataApi) {
    return frameworkDataApi;
  } else return undefined;
};

/**
 * Loads the LkSG-Dataset identified either by the provided reportingPeriod and companyId,
 * or the dataId, and pre-configures the form to contain the data from the dataset
 */
const loadLKSGData = async (): Promise<void> => {
  waitingForData.value = true;
  const lksgDataControllerApi = buildLksgDataApi();
  if (lksgDataControllerApi) {
    let dataResponse;
    if (templateDataId) {
      dataResponse = await lksgDataControllerApi.getFrameworkData(templateDataId.toString());
    } else if (templateReportingPeriod) {
      dataResponse = await lksgDataControllerApi.getCompanyAssociatedDataByDimensions(
        templateReportingPeriod.toString(),
        props.companyID
      );
    }
    if (!dataResponse) {
      waitingForData.value = false;
      throw ReferenceError('DataResponse from LksgDataController invalid.');
    }
    const lksgResponseData = dataResponse.data;
    listOfFilledKpis.value = getFilledKpis(lksgResponseData.data);
    companyAssociatedLksgData.value = objectDropNull(lksgResponseData);
    waitingForData.value = false;
  }
};

/**
 * updates the list of certificates that were uploaded in the corresponding formfields on change
 * @param fieldName the name of the formfield as a key
 * @param document the certificate as combined object of reference id and file content
 */
const updateDocumentList = (fieldName: string, document: DocumentToUpload): void => {
  if (document) {
    fieldSpecificDocuments.value.set(fieldName, document);
  } else {
    fieldSpecificDocuments.value.delete(fieldName);
  }
};

/**
 * Sends data to add LkSG data
 */
const postLkSGData = async (): Promise<void> => {
  messageCounter.value++;
  try {
    if (fieldSpecificDocuments.value.size > 0) {
      await uploadFiles(Array.from(fieldSpecificDocuments.value.values()), assertDefined(getKeycloakPromise));
    }
    const lksgDataControllerApi = buildLksgDataApi();
    if (!lksgDataControllerApi) return;

    const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
      companyAssociatedLksgData.value.companyId,
      getKeycloakPromise
    );

    await lksgDataControllerApi.postFrameworkData(companyAssociatedLksgData.value, isCompanyOwnerOrDataUploader);

    emit('datasetCreated');
    dataDate.value = undefined;
    message.value = 'Upload successfully executed.';
    uploadSucceded.value = true;
  } catch (error) {
    console.error(error);
    if ((error as Error).message) {
      message.value = formatAxiosErrorMessage(error as Error);
    } else {
      message.value =
        'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
    }
    uploadSucceded.value = false;
  } finally {
    postLkSGDataProcessed.value = true;
  }
};

onMounted(() => {
  const reportingPeriod = route.query.reportingPeriod;
  if (reportingPeriod && typeof reportingPeriod === 'string') {
    void loadLKSGData();
  } else {
    waitingForData.value = false;
  }
});

provide(
  'selectedProcurementCategories',
  computed(() => {
    return companyAssociatedLksgData.value.data?.general?.productionSpecificOwnOperations?.procurementCategories;
  })
);

provide('listOfFilledKpis', listOfFilledKpis);
</script>
