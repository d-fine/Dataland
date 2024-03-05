<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - SME</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading SME data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedSmeData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postSmeData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfReportingDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in visibleCategories"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="uploadFormSection grid" v-for="subcategory in category.subcategories" :key="subcategory">
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
                          v-if="field.showIf(companyAssociatedSmeData.data)"
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
                          @reportsUpdated="updateReportsSelection"
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
          <div v-if="postSmeDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in visibleCategories" :key="category">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory">
                  <a
                    v-if="subcategoryVisibilityMap.get(subcategory) ?? true"
                    @click="smoothScroll(`#${category.name}-${subcategory.name}`)"
                    >{{ category.label + ": " + subcategory.label }}</a
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
import { FormKit } from "@formkit/vue";
import { computed, defineComponent, inject } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { useRoute } from "vue-router";
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from "@/utils/ValidationsUtils";
import UploadReports from "@/components/forms/parts/UploadReports.vue";
import { smoothScroll } from "@/utils/SmoothScroll";
import { createSubcategoryVisibilityMap } from "@/utils/UploadFormUtils";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import Calendar from "primevue/calendar";
import type Keycloak from "keycloak-js";
import PrimeButton from "primevue/button";
import { type Category, type Subcategory } from "@/utils/GenericFrameworkTypes";
import { AxiosError } from "axios";
import { type CompanyAssociatedDataSmeData, type CompanyReport, DataTypeEnum } from "@clients/backend";
import { smeDataModel } from "@/frameworks/sme/UploadConfig";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import DateFormField from "@/components/forms/parts/fields/DateFormField.vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import BigDecimalExtendedDataPointFormField from "@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import { type DocumentToUpload, uploadFiles } from "@/utils/FileUploadUtils";
import { type ObjectType } from "@/utils/UpdateObjectUtils";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";
import { getFilledKpis } from "@/utils/DataPoint";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateSmeDataset",
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
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createSmeForm",
      waitingForData: true,
      companyAssociatedSmeData: {} as CompanyAssociatedDataSmeData,
      smeUploadConfig: smeDataModel,
      route: useRoute(),
      message: "",
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postSmeDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      documentsToUpload: [] as DocumentToUpload[],
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      listOfFilledKpis: [] as Array<string>,
    };
  },
  computed: {
    yearOfReportingDate: {
      get(): string {
        const reportingDataInSmeDataset = this.companyAssociatedSmeData.data?.general?.basicInformation?.reportingDate;
        if (reportingDataInSmeDataset === undefined) {
          return "";
        } else {
          return reportingDataInSmeDataset.split("-")[0];
        }
      },
      set() {
        // IGNORED
      },
    },
    visibleCategories(): Category[] {
      return this.smeUploadConfig.filter((category) => category.showIf(this.companyAssociatedSmeData.data));
    },
    subcategoryVisibilityMap(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.smeUploadConfig, this.companyAssociatedSmeData.data);
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
    if (dataId && typeof dataId === "string") {
      void this.loadSmeData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Loads the SME-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadSmeData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const smeDataControllerApi = new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getUnifiedFrameworkDataController(DataTypeEnum.Sme);
      this.companyAssociatedSmeData = (await smeDataControllerApi.getFrameworkData(dataId)).data;
      this.listOfFilledKpis = getFilledKpis(this.companyAssociatedSmeData.data);
      this.referencedReportsForPrefill =
        this.companyAssociatedSmeData.data.general.basicInformation.referencedReports ?? {};
      this.waitingForData = false;
    },
    /**
     * Sends data to add SME data
     */
    async postSmeData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documentsToUpload.length > 0) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedSmeData.data as ObjectType,
            Object.keys(this.namesAndReferencesOfAllCompanyReportsForTheDataset),
          );
          await uploadFiles(this.documentsToUpload, assertDefined(this.getKeycloakPromise));
        }
        const smeDataControllerApi = new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getUnifiedFrameworkDataController(DataTypeEnum.Sme);
        await smeDataControllerApi.postFrameworkData(this.companyAssociatedSmeData);
        this.$emit("datasetCreated");
        this.message = "Upload successfully executed.";
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = "An error occurred: " + error.message;
        } else if (error.message) {
          this.message = formatAxiosErrorMessage(error as Error);
        }
        this.uploadSucceded = false;
      } finally {
        this.postSmeDataProcessed = true;
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
