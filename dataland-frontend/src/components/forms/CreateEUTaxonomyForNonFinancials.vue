<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title
      >{{ isItUploadForm ? "Update" : "Create" }} EU Taxonomy Dataset for a Non-Financial Company/Service
    </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            :id="formId"
            @submit="postEuTaxonomyDataForNonFinancials"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :model-value="companyID"
              disabled="true"
            />
            <div class="uploadFormSection grid">
              <div class="col-3 p-3 topicLabel">
                <h4 id="reportingPeriod" class="anchor title">Reporting Period</h4>
              </div>
              <div class="col-9 formFields uploaded-files">
                <UploadFormHeader
                  :name="euTaxonomyKpiNameMappings.reportingPeriod"
                  :explanation="euTaxonomyKpiInfoMappings.reportingPeriod"
                />
                <div class="lg:col-6 md:col-6 col-12 p-0">
                  <Calendar
                    data-test="reportingPeriod"
                    v-model="reportingPeriod"
                    inputId="icon"
                    :showIcon="true"
                    view="year"
                    dateFormat="yy"
                  />
                </div>

                <FormKit type="hidden" v-model="reportingPeriodYear" name="reportingPeriod" />
              </div>
            </div>

            <div class="uploadFormSection grid">
              <FormKit type="group" name="data" label="data">
                <div class="col-3 p-3 topicLabel">
                  <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
                  <p>Please upload all relevant reports for this dataset in the PDF format.</p>
                </div>
                <!-- Select company reports -->
                <div class="col-9 formFields uploaded-files">
                  <h3 class="mt-0">Select company reports</h3>
                  <FileUpload
                    name="fileUpload"
                    accept=".pdf"
                    @select="onSelectedFiles"
                    :maxFileSize="maxFileSize"
                    invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
                    :auto="true"
                  >
                    <template #header="{ chooseCallback }">
                      <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
                        <div class="flex gap-2">
                          <PrimeButton
                            data-test="upload-files-button"
                            @click="chooseCallback()"
                            icon="pi pi-upload"
                            label="UPLOAD REPORTS"
                          />
                        </div>
                      </div>
                    </template>
                    <template #content="{ uploadedFiles, removeUploadedFileCallback }">
                      <div v-if="uploadedFiles.length > 0" data-test="uploaded-files">
                        <div
                          v-for="(file, index) of files.files"
                          :key="file.name + file.reportDate"
                          class="flex w-full align-items-center file-upload-item"
                        >
                          <span data-test="uploaded-files-title" class="font-semibold flex-1">{{ file.name }}</span>
                          <div data-test="uploaded-files-size" class="mx-2 text-black-alpha-50">
                            {{ formatBytesUserFriendly(Number(file.size), 3) }}
                          </div>
                          <PrimeButton
                            data-test="uploaded-files-remove"
                            icon="pi pi-times"
                            @click="files.removeReportFromFilesUploaded(file, removeUploadedFileCallback, index)"
                            class="p-button-rounded"
                          />
                        </div>
                      </div>
                    </template>
                  </FileUpload>
                </div>
                <div class="uploadFormSection">
                  <FormKit name="referencedReports" type="group">
                    <!-- Select company reports -->
                    <div v-for="(file, index) of files.files" :key="file.name" class="col-9 formFields">
                      <div class="form-field-label">
                        <h3 class="mt-0">{{ file.name }}</h3>
                      </div>
                      <FormKit :name="file.name.split('.')[0]" type="group">
                        <!-- Date of the report -->
                        <div class="form-field">
                          <UploadFormHeader
                            :name="euTaxonomyKpiNameMappings.reportDate"
                            :explanation="euTaxonomyKpiInfoMappings.reportDate"
                          />
                          <div class="lg:col-6 md:col-6 col-12 p-0">
                            <Calendar
                              data-test="reportDate"
                              v-model="files.files[index].reportDate"
                              inputId="icon"
                              :showIcon="true"
                              dateFormat="D, M dd, yy"
                              @update:modelValue="updateReportDateHandler(index)"
                            />
                          </div>

                          <FormKit
                            type="text"
                            v-model="files.files[index].convertedReportDate"
                            name="reportDate"
                            :outer-class="{ 'hidden-input': true }"
                          />
                        </div>

                        <FormKit
                          type="text"
                          v-model="files.filesNames[index]"
                          name="reference"
                          :outer-class="{ 'hidden-input': true }"
                        />

                        <!-- Currency used in the report -->
                        <div class="form-field" data-test="currencyUsedInTheReport">
                          <UploadFormHeader
                            :name="euTaxonomyKpiNameMappings.currency"
                            :explanation="euTaxonomyKpiInfoMappings.currency"
                            :is-required="true"
                          />
                          <div class="lg:col-4 md:col-4 col-12 p-0">
                            <FormKit
                              type="text"
                              name="currency"
                              validation="required|length:2,3"
                              validation-label="Currency used in the report"
                              placeholder="Currency used in the report"
                            />
                          </div>
                        </div>
                        <!-- Integrated report is on a group level -->
                        <div class="form-field">
                          <YesNoComponent
                            :displayName="euTaxonomyKpiNameMappings.groupLevelIntegratedReport"
                            :info="euTaxonomyKpiInfoMappings.groupLevelIntegratedReport"
                            :name="'isGroupLevel'"
                          />
                        </div>
                      </FormKit>
                    </div>
                  </FormKit>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="basicInformation" class="anchor title">Basic information</h4>
                  </div>
                  <!-- Basic information -->
                  <div class="col-9 formFields">
                    <h3 class="mt-0">Basic information</h3>

                    <YesNoComponent
                      :displayName="euTaxonomyKpiNameMappings.fiscalYearDeviation"
                      :info="euTaxonomyKpiInfoMappings.fiscalYearDeviation"
                      :name="'fiscalYearDeviation'"
                      :radioButtonsOptions="['Deviation', 'No Deviation']"
                      required="required"
                    />

                    <!-- The date the fiscal year ends -->
                    <div class="form-field">
                      <UploadFormHeader
                        :name="euTaxonomyKpiNameMappings.fiscalYearEnd"
                        :explanation="euTaxonomyKpiInfoMappings.fiscalYearEnd"
                        :is-required="true"
                      />
                      <div class="lg:col-6 md:col-6 col-12 p-0">
                        <Calendar
                          inputId="fiscalYearEnd"
                          v-model="fiscalYearEnd"
                          data-test="fiscalYearEnd"
                          :showIcon="true"
                          dateFormat="D, M dd, yy"
                        />
                      </div>

                      <FormKit
                        type="text"
                        validation="required"
                        validation-label="Fiscal year"
                        name="fiscalYearEnd"
                        v-model="convertedFiscalYearEnd"
                        :outer-class="{ 'hidden-input': true }"
                      />
                    </div>

                    <!-- Scope of entities -->
                    <div class="form-field">
                      <YesNoComponent
                        :displayName="euTaxonomyKpiNameMappings.scopeOfEntities"
                        :info="euTaxonomyKpiInfoMappings.scopeOfEntities"
                        :name="'scopeOfEntities'"
                      />
                    </div>

                    <!-- EU Taxonomy activity level reporting -->
                    <div class="form-field">
                      <YesNoComponent
                        :displayName="euTaxonomyKpiNameMappings.activityLevelReporting"
                        :info="euTaxonomyKpiInfoMappings.activityLevelReporting"
                        :name="'activityLevelReporting'"
                      />
                    </div>

                    <!-- Number of employees -->
                    <div class="form-field">
                      <UploadFormHeader
                        :name="euTaxonomyKpiNameMappings.numberOfEmployees"
                        :explanation="euTaxonomyKpiInfoMappings.numberOfEmployees"
                        :is-required="true"
                      />
                      <div class="lg:col-4 md:col-4 col-6 p-0">
                        <FormKit
                          type="number"
                          name="numberOfEmployees"
                          validation-label="Number of employees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                        />
                      </div>
                    </div>

                    <!-- EU Taxonomy activity level reporting -->
                    <div class="form-field">
                      <YesNoComponent
                        :displayName="euTaxonomyKpiNameMappings.reportingObligation"
                        :info="euTaxonomyKpiInfoMappings.reportingObligation"
                        :name="'reportingObligation'"
                      />
                    </div>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="assurance" class="anchor title">Assurance</h4>
                  </div>

                  <!-- Level of assurance -->
                  <div data-test="assuranceSection" class="col-9 formFields">
                    <h3 class="mt-0">Assurance</h3>
                    <FormKit name="assurance" type="group">
                      <!-- Level of assurance -->
                      <div class="form-field">
                        <UploadFormHeader
                          :name="euTaxonomyKpiNameMappings.assurance ?? ''"
                          :explanation="euTaxonomyKpiInfoMappings.assurance ?? ''"
                          :is-required="true"
                        />
                        <div class="lg:col-4 md:col-6 col-12 p-0">
                          <FormKit
                            type="select"
                            name="assurance"
                            placeholder="Please choose..."
                            :validation-label="euTaxonomyKpiNameMappings.assurance ?? ''"
                            validation="required"
                            :options="assuranceData"
                          />
                        </div>
                      </div>
                      <!-- Assurance provider -->
                      <div class="form-field">
                        <UploadFormHeader
                          :name="euTaxonomyKpiNameMappings.provider ?? ''"
                          :explanation="euTaxonomyKpiInfoMappings.provider ?? ''"
                        />
                        <FormKit
                          type="text"
                          name="provider"
                          :placeholder="euTaxonomyKpiNameMappings.provider ?? ''"
                          :validation-label="euTaxonomyKpiNameMappings.provider ?? ''"
                        />
                      </div>

                      <!-- Data source -->
                      <div class="form-field">
                        <FormKit type="group" name="dataSource">
                          <h4 class="mt-0">Data source</h4>
                          <div class="next-to-each-other">
                            <div class="flex-1">
                              <UploadFormHeader
                                :name="euTaxonomyKpiNameMappings.report ?? ''"
                                :explanation="euTaxonomyKpiInfoMappings.report ?? ''"
                                :is-required="true"
                              />
                              <FormKit
                                type="select"
                                name="report"
                                placeholder="Select a report"
                                validation-label="Select a report"
                                validation="required"
                                :options="['None...', ...this.files.filesNames]"
                              />
                            </div>
                            <div>
                              <UploadFormHeader
                                :name="euTaxonomyKpiNameMappings.page ?? ''"
                                :explanation="euTaxonomyKpiInfoMappings.page ?? ''"
                              />
                              <FormKit
                                outer-class="w-100"
                                type="number"
                                name="page"
                                placeholder="Page"
                                validation-label="Page"
                                step="1"
                                min="0"
                              />
                            </div>
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="capex" class="anchor title">CapEx</h4>
                  </div>

                  <!-- CapEx -->
                  <div data-test="capexSection" class="col-9 p-0">
                    <FormKit name="capex" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}CapEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="opex" class="anchor title">OpEx</h4>
                  </div>

                  <!-- OpEx -->
                  <div data-test="opexSection" class="col-9 p-0">
                    <FormKit name="opex" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}OpEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="revenue" class="anchor title">Revenue</h4>
                  </div>

                  <!-- Revenue -->
                  <div data-test="revenueSection" class="col-9 p-0">
                    <FormKit name="revenue" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}Revenue`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </div>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <template v-if="postEuTaxonomyDataForNonFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForNonFinancialsResponse"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailedUpload v-else msg="EU Taxonomy Data" :messageId="messageCount" />
          </template>
          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="(element, index) in onThisPageLinks" :key="index">
              <a @click="smoothScroll(`#${element.value}`)">{{ element.label }}</a>
            </li>
          </ul>
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";

import Calendar from "primevue/calendar";
import { useFilesUploadedStore } from "@/stores/filesUploaded";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import PrimeButton from "primevue/button";
import FileUpload from "primevue/fileupload";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";

import FailedUpload from "@/components/messages/FailedUpload.vue";
import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeString } from "@/utils/StringHumanizer";
import { defineComponent, inject } from "vue";
import { useRoute } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyKPIsModel,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { CompanyAssociatedDataEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import { smoothScroll } from "@/utils/smoothScroll";
import { checkCustomInputs } from "@/utils/validationsUtils";
import { modifyObjectKeys, objectType, updateObject } from "@/utils/updateObjectUtils";
import DataPointForm from "@/components/forms/parts/kpiSelection/DataPointForm.vue";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";

export default defineComponent({
  name: "CreateEUTaxonomyForNonFinancials",
  components: {
    SubmitButton,
    DataPointForm,
    Calendar,
    UploadFormHeader,
    PrimeButton,
    FileUpload,
    YesNoComponent,
    FailedUpload,
    SubmitSideBar,
    Card,
    FormKit,
    SuccessUpload,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  emits: ["datasetCreated"],
  data: () => ({
    formId: "createEuTaxonomyForNonFinancialsForm",
    formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
    files: useFilesUploadedStore(),
    fiscalYearEnd: undefined as Date | undefined,
    convertedFiscalYearEnd: "",
    reportingPeriod: new Date(),
    onThisPageLinks: [
      { label: "Upload company reports", value: "uploadReports" },
      { label: "Basic information", value: "basicInformation" },
      { label: "Assurance", value: "assurance" },
      { label: "CapEx", value: "capex" },
      { label: "OpEx", value: "opex" },
      { label: "Revenue", value: "revenue" },
    ],
    isItUploadForm: false,
    route: useRoute(),
    waitingForData: false,
    formatBytesUserFriendly,
    smoothScroll,
    checkCustomInputs,
    maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
    euTaxonomyKPIsModel,
    euTaxonomyKpiNameMappings,
    euTaxonomyKpiInfoMappings,
    reportingPeriodYear: new Date().getFullYear(),
    assuranceData: {
      None: humanizeString("None"),
      LimitedAssurance: humanizeString("LimitedAssurance"),
      ReasonableAssurance: humanizeString("ReasonableAssurance"),
    },

    postEuTaxonomyDataForNonFinancialsProcessed: false,
    messageCount: 0,
    postEuTaxonomyDataForNonFinancialsResponse: null,
    humanizeString: humanizeString,
  }),
  watch: {
    reportingPeriod: function (newValue: Date) {
      this.reportingPeriodYear = newValue.getFullYear();
    },
    fiscalYearEnd: function (newValue: Date) {
      if (newValue) {
        this.convertedFiscalYearEnd = getHyphenatedDate(newValue);
      } else {
        this.convertedFiscalYearEnd = "";
      }
    },
  },
  props: {
    companyID: {
      type: String,
    },
  },
  mounted() {
    const dataId = this.route.query.templateDataId;
    if (dataId !== undefined && typeof dataId === "string" && dataId !== "") {
      this.isItUploadForm = true;
      void this.loadEuData(dataId);
    }
  },
  methods: {
    /**
     * Loads the Dataset by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadEuData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getEuTaxonomyDataForNonFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId);
      const dataResponseData = dataResponse.data;
      if (dataResponseData.data?.fiscalYearEnd) {
        this.fiscalYearEnd = new Date(dataResponseData.data.fiscalYearEnd);
      }
      const receivedFormInputsModel = modifyObjectKeys(
        JSON.parse(JSON.stringify(dataResponseData)) as objectType,
        "receive"
      );
      updateObject(this.formInputsModel, receivedFormInputsModel);
      this.waitingForData = false;
    },
    /**
     * Creates a new EuTaxonomy-Non-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForNonFinancials() {
      try {
        this.postEuTaxonomyDataForNonFinancialsProcessed = false;
        this.messageCount++;
        const formInputsModelToSend = modifyObjectKeys(
          JSON.parse(JSON.stringify(this.formInputsModel)) as objectType,
          "send"
        );
        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        this.postEuTaxonomyDataForNonFinancialsResponse =
          await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            formInputsModelToSend
          );
        this.$emit("datasetCreated");
        this.$formkit.reset(this.formId);
      } catch (error) {
        this.postEuTaxonomyDataForNonFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },

    /**
     * Updates the date of a single report file
     * @param index file to update
     */
    updateReportDateHandler(index: number) {
      this.files.updatePropertyFilesUploaded(
        index,
        "convertedReportDate",
        getHyphenatedDate(this.files.files[index].reportDate as unknown as Date)
      );
    },

    /**
     * Modifies the file object and adds it to the store
     * @param event date in date format
     * @param event.originalEvent event
     * @param event.files files
     */
    onSelectedFiles(event: { files: Record<string, string>[]; originalEvent: Event }): void {
      if (event.files.length) {
        event.files[0]["reportDate"] = "";
        event.files[0]["convertedReportDate"] = "";
        this.files.setReportsFilesUploaded(event.files[0]);
      } else {
        return;
      }
    },
  },
});
</script>
