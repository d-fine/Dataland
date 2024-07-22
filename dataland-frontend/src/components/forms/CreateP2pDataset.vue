<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - P2P</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading P2P data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedP2pData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postP2pData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in visibleCategories"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="uploadFormSection grid" v-for="subcategory in category.subcategories" :key="subcategory">
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="col-3 p-3 topicLabel">
                      <h4 :id="`${category.name}-${subcategory.name}`" class="anchor title">{{ subcategory.label }}</h4>
                      <div :class="`p-badge badge-${category.color}`">
                        <span>{{ category.label.toUpperCase() }}</span>
                      </div>
                    </div>

                    <div class="col-9 formFields">
                      <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                        <component
                          v-if="field.showIf(companyAssociatedP2pData.data)"
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
                          @field-specific-documents-updated="
                            updateDocumentList(`${category.name}.${subcategory.name}.${field.name}`, $event)
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
          <div v-if="postP2pDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in visibleCategories" :key="category">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory">
                  <a
                    v-if="subcategoryVisibility.get(subcategory) ?? true"
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
// @ts-nocheck
import { FormKit } from '@formkit/vue';
import { computed, defineComponent, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { useRoute } from 'vue-router';
import { checkCustomInputs } from '@/utils/ValidationsUtils';
import { objectDropNull, type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import Calendar from 'primevue/calendar';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { type DriveMixType } from '@/api-models/DriveMixType';
import { type Category, type Subcategory } from '@/utils/GenericFrameworkTypes';
import { AxiosError } from 'axios';
import { type CompanyAssociatedDataPathwaysToParisData, DataTypeEnum, type P2pDriveMix } from '@clients/backend';
import { p2pDataModel } from '@/components/resources/frameworkDataSearch/p2p/P2pDataModel';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import DriveMixFormField from '@/components/forms/parts/fields/DriveMixFormField.vue';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoNaFormField from '@/components/forms/parts/fields/YesNoNaFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import { type DocumentToUpload, uploadFiles } from '@/utils/FileUploadUtils';
import { getFilledKpis } from '@/utils/DataPoint';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateP2pDataset',
  components: {
    DriveMixFormField,
    FormKit,
    UploadFormHeader,
    InputTextFormField,
    MultiSelectFormField,
    NumberFormField,
    PercentageFormField,
    Card,
    PrimeButton,
    Calendar,
    SuccessMessage,
    FailMessage,
    SubmitButton,
    SubmitSideBar,
    DateFormField,
    SingleSelectFormField,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyDataPointFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createP2pForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedP2pData: {} as CompanyAssociatedDataPathwaysToParisData,
      p2pDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postP2pDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      fieldSpecificDocuments: new Map() as Map<string, DocumentToUpload>,
      listOfFilledKpis: [] as Array<string>,
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedP2pData.data?.general?.general?.dataDate;
        if (currentDate === undefined) {
          return '';
        } else {
          return currentDate.split('-')[0];
        }
      },
      set() {
        // IGNORED
      },
    },
    visibleCategories(): Category[] {
      return this.p2pDataModel.filter(
        (category) => category.showIf(this.companyAssociatedP2pData.data) || category.name === 'general'
      );
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.p2pDataModel, this.companyAssociatedP2pData.data);
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
      void this.loadP2pData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Loads the P2p-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadP2pData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const p2pDataControllerApi = new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getUnifiedFrameworkDataController(DataTypeEnum.P2p);

      const p2pDataset = (await p2pDataControllerApi.getFrameworkData(dataId)).data;
      this.listOfFilledKpis = getFilledKpis(p2pDataset.data);
      const dataDateFromDataset = p2pDataset.data?.general?.general?.dataDate;
      if (dataDateFromDataset) {
        this.dataDate = new Date(dataDateFromDataset);
      }
      this.companyAssociatedP2pData = objectDropNull(
        p2pDataset as ObjectType
      ) as CompanyAssociatedDataPathwaysToParisData;
      this.waitingForData = false;
    },
    /**
     * Sends data to add P2p data
     */
    async postP2pData(): Promise<void> {
      this.messageCounter++;
      try {
        this.filterFieldSpecificDocumentsByEnabledFieldOwnership();
        if (this.fieldSpecificDocuments.size > 0) {
          await uploadFiles(Array.from(this.fieldSpecificDocuments.values()), assertDefined(this.getKeycloakPromise));
        }
        const p2pDataControllerApi = new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getUnifiedFrameworkDataController(DataTypeEnum.P2p);

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedP2pData.companyId,
          this.getKeycloakPromise
        );

        await p2pDataControllerApi.postFrameworkData(this.companyAssociatedP2pData, isCompanyOwnerOrDataUploader);

        this.$emit('datasetCreated');
        this.dataDate = undefined;
        this.message = 'Upload successfully executed.';
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = 'An error occurred: ' + error.message;
        } else {
          this.message =
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
        this.uploadSucceded = false;
      } finally {
        this.postP2pDataProcessed = true;
      }
    },

    /**
     * Filters the field specific documents for documents that belong to fields that are not enabled
     */
    filterFieldSpecificDocumentsByEnabledFieldOwnership() {
      Array.from(this.fieldSpecificDocuments.keys()).forEach((key) => {
        const categoryName = key.split('.')[0];
        const allowedCategories = this.companyAssociatedP2pData.data.general.general.sectors
          .map((sector) => sector.toString().toLowerCase())
          .concat('general');
        if (!allowedCategories.includes(categoryName.toLowerCase())) {
          this.fieldSpecificDocuments.delete(key);
        }
      });
    },

    /**
     * updates the list of certificates that were uploaded in the corresponding formfields on change
     * @param fieldName the name of the formfield as a key
     * @param document the certificate as combined object of reference id and file content
     */
    updateDocumentList(fieldName: string, document: DocumentToUpload) {
      if (document) {
        this.fieldSpecificDocuments.set(fieldName, document);
      } else {
        this.fieldSpecificDocuments.delete(fieldName);
      }
    },
  },
  provide() {
    return {
      driveMixPerFleetSegment: computed(() => {
        return this.companyAssociatedP2pData.data?.freightTransportByRoad?.technology?.driveMixPerFleetSegment as Map<
          DriveMixType,
          P2pDriveMix
        > | null;
      }),
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
