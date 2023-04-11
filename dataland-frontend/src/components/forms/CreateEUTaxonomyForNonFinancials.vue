<template>
  <Card class="col-12 page-wrapper-card">
    <template #title
      ><span data-test="pageWrapperTitle"
        >{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Non-Financial Company/Service</span
      ></template
    >
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createEuTaxonomyForNonFinancialsForm"
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
                <div class="md:col-6 col-12 p-0">
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
                <UploadReports
                  ref="UploadReports"
                  :filesToUpload="filesToUpload"
                  :uploadFiles="uploadFiles"
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :maxFileSize="maxFileSize"
                  :editMode="editMode"
                  @selectedFiles="onSelectedFilesHandler"
                  @removeReportFromFilesToUpload="removeReportFromFilesToUpload"
                  @updateReportDateHandler="updateReportDateHandler"
                />

                <BasicInformationFields
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :fiscalYearEnd="fiscalYearEnd"
                  :convertedFiscalYearEnd="convertedFiscalYearEnd"
                  @updateFiscalYearEndHandler="updateFiscalYearEndHandler"
                />

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
                        />
                        <div class="lg:col-4 md:col-6 col-12 p-0">
                          <FormKit
                            type="select"
                            name="assurance"
                            placeholder="Please chose..."
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
                              />
                              <FormKit
                                type="select"
                                name="report"
                                placeholder="Select a report"
                                validation-label="Select a report"
                                validation="required"
                                :options="['None...', ...namesOfFilesToUpload]"
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
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}CapEx`] ?? "" }}
                            </h3>
                            <KPIfieldSet
                              :name="`${detailCashFlowType}CapEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :toggleDataAvailable="false"
                              :valueType="detailCashFlowType === 'totalAmount' ? 'number' : 'percent'"
                              :reportsName="namesOfFilesToUpload"
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
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}OpEx`] ?? "" }}
                            </h3>
                            <KPIfieldSet
                              :name="`${detailCashFlowType}OpEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :toggleDataAvailable="false"
                              :valueType="detailCashFlowType === 'totalAmount' ? 'number' : 'percent'"
                              :reportsName="namesOfFilesToUpload"
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
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}Revenue`] ?? "" }}
                            </h3>
                            <KPIfieldSet
                              :name="`${detailCashFlowType}Revenue`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :toggleDataAvailable="false"
                              :valueType="detailCashFlowType === 'totalAmount' ? 'number' : 'percent'"
                              :reportsName="namesOfFilesToUpload"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <!--------- SUBMIT --------->

              <div class="uploadFormSection grid">
                <div class="col-3"></div>

                <div class="col-9">
                  <PrimeButton data-test="submitButton" type="submit" label="SUBMIT FORM" />
                </div>
              </div>
            </div>
          </FormKit>
          <template v-if="postEuTaxonomyDataForNonFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForNonFinancialsResponse"
              msg="EU Taxonomy Data"
              :message="postEuTaxonomyDataForNonFinancialsResponse.data"
              :messageId="messageCount"
            />
            <FailedUpload v-else msg="EU Taxonomy Data" :messageId="messageCount" />
          </template>
        </div>
        <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";

import Calendar from "primevue/calendar";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import PrimeButton from "primevue/button";

import KPIfieldSet from "@/components/forms/parts/kpiSelection/KPIfieldSet.vue";

import FailedUpload from "@/components/messages/FailedUpload.vue";
import UploadReports from "@/components/forms/parts/UploadReports.vue";
import BasicInformationFields from "@/components/forms/parts/BasicInformationFields.vue";

import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeString } from "@/utils/StringHumanizer";
import { defineComponent, inject } from "vue";
import { useRoute } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

import {
  euTaxonomyKPIsModel,
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/euTaxonomyKPIsModel";
import { CompanyAssociatedDataEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import { checkCustomInputs } from "@/utils/validationsUtils";
import { modifyObjectKeys, objectType, updateObject } from "@/utils/updateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { ExtendedCompanyReport, ExtendedFile, WhichSetOfFiles } from "@/components/forms/Types";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";

export default defineComponent({
  name: "CreateEUTaxonomyForNonFinancials",
  components: {
    JumpLinksSection,
    Calendar,
    UploadFormHeader,
    PrimeButton,
    UploadReports,
    BasicInformationFields,
    KPIfieldSet,
    FailedUpload,
    Card,
    FormKit,
    SuccessUpload,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data: () => ({
    formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
    fiscalYearEnd: "" as Date | "",
    convertedFiscalYearEnd: "",
    reportingPeriod: new Date(),
    filesToUpload: [] as ExtendedFile[],
    uploadFiles: [] as ExtendedCompanyReport[],
    onThisPageLinks: [
      { label: "Upload company reports", value: "uploadReports" },
      { label: "Basic information", value: "basicInformation" },
      { label: "Assurance", value: "assurance" },
      { label: "CapEx", value: "capex" },
      { label: "OpEx", value: "opex" },
      { label: "Revenue", value: "revenue" },
    ],
    elementPosition: 0,
    route: useRoute(),
    editMode: false,
    waitingForData: false,
    checkCustomInputs,
    formatBytesUserFriendly,
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
  },
  computed: {
    namesOfFilesToUpload(): string[] {
      const namesFromFilesToUpload = this.filesToUpload.map((el) => el.name.split(".")[0]);
      const namesFromUploadedFiles = this.uploadFiles.map((el) => el.name.split(".")[0]);
      return [...new Set([...namesFromFilesToUpload, ...namesFromUploadedFiles])];
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
      this.editMode = true;
      void this.loadEuData(dataId);
    }
  },

  methods: {
    /**
     * Loads the Dataset by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     *
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
      if (dataResponseData.data?.referencedReports) {
        const propertiesOfFilesAssignedToDataID = dataResponseData.data.referencedReports;
        for (const key in propertiesOfFilesAssignedToDataID) {
          this.uploadFiles.push({
            name: key,
            currency: propertiesOfFilesAssignedToDataID[key].currency,
            isGroupLevel: propertiesOfFilesAssignedToDataID[key].isGroupLevel,
            reference: propertiesOfFilesAssignedToDataID[key].reference,
            reportDate: propertiesOfFilesAssignedToDataID[key].reportDate,
            convertedReportDate: propertiesOfFilesAssignedToDataID[key].reportDate
              ? new Date(propertiesOfFilesAssignedToDataID[key].reportDate as string)
              : "",
          });
        }
      }
      const receivedFormInputsModel = modifyObjectKeys(
        JSON.parse(JSON.stringify(dataResponseData)) as objectType,
        "receive"
      );
      this.waitingForData = false;
      updateObject(this.formInputsModel, receivedFormInputsModel);
    },

    /**
     * Creates a new EuTaxonomy-Non-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForNonFinancials() {
      try {
        this.postEuTaxonomyDataForNonFinancialsProcessed = false;
        this.messageCount++;
        let allFileUploadedSuccessful = true;

        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();

        const documentUploadControllerControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentUploadController();

        if (this.filesToUpload.length) {
          for (let index = 0; index < this.filesToUpload.length; index++) {
            const uploadFileSuccessful = await documentUploadControllerControllerApi.postDocument(
              this.filesToUpload[index]
            );
            if (!uploadFileSuccessful) {
              allFileUploadedSuccessful = false;
              break;
            } else if (uploadFileSuccessful) {
              this.updatePropertyFilesUploaded(
                index,
                "documentId",
                uploadFileSuccessful.data.documentId,
                "filesToUpload"
              );
            }
          }
        }

        if (allFileUploadedSuccessful) {
          await this.$nextTick();
          const formInputsModelToSend = modifyObjectKeys(
            JSON.parse(JSON.stringify(this.formInputsModel)) as objectType,
            "send"
          );
          this.postEuTaxonomyDataForNonFinancialsResponse =
            await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
              formInputsModelToSend
            );
        }
        this.$formkit.reset("createEuTaxonomyForNonFinancialsForm");
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.UploadReports.clearAllNotUploadedFiles();
        this.fiscalYearEnd = "";
        this.filesToUpload = [];
      } catch (error) {
        this.postEuTaxonomyDataForNonFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },

    /**
     * Updates the Fiscal Year End value
     *
     * @param dateValue new date value
     */
    updateFiscalYearEndHandler(dateValue: Date) {
      this.convertedFiscalYearEnd = getHyphenatedDate(dateValue);
      this.fiscalYearEnd = dateValue;
    },

    /**
     * Updates the date of a single report file
     *
     * @param index file to update
     * @param dateValue new date value
     * @param whichSetOfFiles which set of files will be edited
     */
    updateReportDateHandler(index: number, dateValue: Date, whichSetOfFiles: WhichSetOfFiles) {
      this.updatePropertyFilesUploaded(index, "convertedReportDate", dateValue, whichSetOfFiles);
      this.updatePropertyFilesUploaded(index, "reportDate", getHyphenatedDate(dateValue), whichSetOfFiles);
    },

    /**
     * Add files to object filesToUpload
     *
     * @param event date in date format
     * @param event.originalEvent event
     * @param event.files files
     */
    onSelectedFilesHandler(event: { files: Record<string, string>[]; originalEvent: Event }): void {
      if (event.files.length) {
        event.files.forEach((file) => {
          if (this.uploadFiles.some((objfile) => objfile.name === file.name.split(".")[0])) {
            file["nameAlreadyExists"] = "true";
          } else {
            file["reportDate"] = file["reportDate"] ?? "";
            file["convertedReportDate"] = file["convertedReportDate"] ?? "";
            file["documentId"] = file["documentId"] ?? "";
          }
        });

        this.filesToUpload = [...event.files] as ExtendedFile[];
      } else {
        return;
      }
    },

    /**
     * Update property in uploaded files
     *
     * @param indexFileToUpload Index number of the report
     * @param property Property which is to be updated
     * @param value Value to which it is to be changed
     * @param whichSetOfFiles which set of files will be edited
     */
    updatePropertyFilesUploaded(
      indexFileToUpload: number,
      property: string,
      value: string | Date,
      whichSetOfFiles: WhichSetOfFiles
    ) {
      if (Object.prototype.hasOwnProperty.call(this[whichSetOfFiles][indexFileToUpload], property)) {
        this[whichSetOfFiles][indexFileToUpload][property] = value;
        this[whichSetOfFiles] = [...this[whichSetOfFiles]];
      }
    },

    /**
     * Remove report from files uploaded
     *
     * @param fileToRemove File To Remove
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param index Index number of the report
     */
    removeReportFromFilesToUpload(
      fileToRemove: Record<string, string>,
      fileRemoveCallback: (x: number) => void,
      index: number
    ) {
      fileRemoveCallback(index);
      this.filesToUpload = this.filesToUpload.filter((el) => {
        return el.name !== fileToRemove.name;
      });
    },
  },
});
</script>

// TODO the back button is missing. in the CreateEuTaxonomy for financials it is there
