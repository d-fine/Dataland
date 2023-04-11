<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Financial Company/Service</template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createEuTaxonomyForFinancialsForm"
            @submit="postEuTaxonomyDataForFinancials"
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
                  data-test="reportingPeriodLabel"
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
                    <h4 id="addKpis" class="anchor title">Add KPIs</h4>
                    <p>Select at least one services type to add the related section of KPIs.</p>
                  </div>

                  <!-- Add KPIs -->
                  <div class="col-9 formFields">
                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader
                        data-test="selectKPIsLabel"
                        :name="euTaxonomyKpiNameMappings.financialServicesTypes ?? ''"
                        :explanation="euTaxonomyKpiInfoMappings.financialServicesTypes ?? ''"
                      />

                      <MultiSelect
                        v-model="selectedKPIs"
                        :options="kpisModel"
                        data-test="MultiSelectfinancialServicesTypes"
                        name="MultiSelectfinancialServicesTypes"
                        optionLabel="label"
                        validation-label="Services Types"
                        validation="required"
                        placeholder="Select..."
                        class="mb-3"
                      />
                      <ul v-if="selectedKPIs.length">
                        <li :key="index" v-for="(file, index) of selectedKPIs">{{ file.label }}</li>
                      </ul>

                      <PrimeButton
                        @click="confirmeSelectedKPIs"
                        data-test="addKpisButton"
                        :label="selectedKPIs.length ? 'UPDATE KPIS' : 'ADD RELATED KPIS'"
                      />
                      <FormKit
                        v-model="computedFinancialServicesTypes"
                        type="text"
                        validationLabel="You must choose and confirm"
                        validation="required"
                        name="financialServicesTypes"
                        :outer-class="{ 'hidden-input': true }"
                      />
                    </div>
                  </div>
                </div>

                <div
                  v-for="copanyType of confirmedSelectedKPIs"
                  :key="copanyType"
                  :data-test="copanyType.value"
                  class="uploadFormSection"
                >
                  <div class="flex w-full">
                    <div class="p-3 topicLabel">
                      <h3 :id="copanyType.value" class="anchor title">{{ copanyType.label }}</h3>
                    </div>

                    <PrimeButton
                      @click="removeKpisSection(copanyType.value)"
                      label="REMOVE THIS SECTION"
                      data-test="removeSectionButton"
                      class="p-button-text ml-auto"
                      icon="pi pi-trash"
                    ></PrimeButton>
                  </div>

                  <FormKit v-if="copanyType.value !== 'assetManagementKpis'" :name="copanyType.value" type="group">
                    <div
                      v-for="kpiType of euTaxonomyKPIsModel[copanyType.value]"
                      :key="kpiType"
                      :data-test="kpiType"
                      class="uploadFormSection"
                    >
                      <div class="col-9 formFields">
                        <FormKit :name="kpiType" type="group">
                          <div class="form-field">
                            <UploadFormHeader
                              :name="euTaxonomyKpiNameMappings[kpiType] ?? ''"
                              :explanation="euTaxonomyKpiInfoMappings[kpiType] ?? ''"
                            />
                            <KPIfieldSet
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :reportsName="namesOfFilesToUpload"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </div>
                  </FormKit>

                  <FormKit name="eligibilityKpis" type="group">
                    <FormKit :name="euTaxonomyKPIsModel?.companyTypeToEligibilityKpis[copanyType.value]" type="group">
                      <div
                        v-for="kpiTypeEligibility of euTaxonomyKPIsModel.eligibilityKpis"
                        :key="kpiTypeEligibility"
                        :data-test="kpiTypeEligibility"
                        class="uploadFormSection"
                      >
                        <div class="col-9 formFields">
                          <FormKit :name="kpiTypeEligibility" type="group">
                            <div class="form-field">
                              <UploadFormHeader
                                :name="euTaxonomyKpiNameMappings[kpiTypeEligibility] ?? ''"
                                :explanation="euTaxonomyKpiInfoMappings[kpiTypeEligibility] ?? ''"
                              />
                              <KPIfieldSet
                                :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                                :kpiNameMappings="euTaxonomyKpiNameMappings"
                                :reportsName="namesOfFilesToUpload"
                              />
                            </div>
                          </FormKit>
                        </div>
                      </div>
                    </FormKit>
                  </FormKit>
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
          <template v-if="postEuTaxonomyDataForFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForFinancialsResponse"
              msg="EU Taxonomy Data"
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

import UploadReports from "@/components/forms/parts/UploadReports.vue";
import BasicInformationFields from "@/components/forms/parts/BasicInformationFields.vue";

import PrimeButton from "primevue/button";
import MultiSelect from "primevue/multiselect";
import KPIfieldSet from "@/components/forms/parts/kpiSelection/KPIfieldSet.vue";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import { useRoute } from "vue-router";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { checkCustomInputs } from "@/utils/validationsUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import {
  euTaxonomyKPIsModel,
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/euTaxonomyKPIsModel";
import {
  CompanyAssociatedDataEuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  DataMetaInformation,
} from "@clients/backend";
import { AxiosResponse } from "axios";
import { modifyObjectKeys, objectType, updateObject } from "@/utils/updateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { ExtendedCompanyReport, ExtendedFile, WhichSetOfFiles } from "@/components/forms/Types";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateEUTaxonomyForFinancials",
  components: {
    JumpLinksSection,
    FailedUpload,
    FormKit,
    SuccessUpload,
    UploadFormHeader,
    Card,

    UploadReports,
    BasicInformationFields,

    PrimeButton,
    Calendar,
    MultiSelect,
    KPIfieldSet,
  },

  data() {
    return {
      formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForFinancials,
      filesToUpload: [] as ExtendedFile[],
      uploadFiles: [] as ExtendedCompanyReport[],
      fiscalYearEnd: "" as Date | "",
      convertedFiscalYearEnd: "",
      reportingPeriod: new Date(),
      assuranceData: [
        { label: "None", value: "None" },
        { label: "LimitedAssurance", value: "LimitedAssurance" },
        { label: "ReasonableAssurance", value: "ReasonableAssurance" },
      ],
      maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      euTaxonomyKPIsModel,
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      checkCustomInputs,
      formatBytesUserFriendly,
      route: useRoute(),
      waitingForData: false,
      editMode: false,

      postEuTaxonomyDataForFinancialsProcessed: false,
      messageCount: 0,
      postEuTaxonomyDataForFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
      humanizeString: humanizeString,
      onThisPageLinksStart: [
        { label: "Upload company reports", value: "uploadReports" },
        { label: "Basic information", value: "basicInformation" },
        { label: "Assurance", value: "assurance" },
        { label: "Add KPIs", value: "addKpis" },
      ],
      onThisPageLinks: [] as { label: string; value: string }[],
      kpisModel: [
        { label: "Credit Institution", value: "creditInstitutionKpis" },
        { label: "Investment Firm", value: "investmentFirmKpis" },
        { label: "Insurance & Re-insurance", value: "insuranceKpis" },
        { label: "Asset Management", value: "assetManagementKpis" },
      ],
      selectedKPIs: [] as { label: string; value: string }[],
      confirmedSelectedKPIs: [] as { label: string; value: string }[],
      computedFinancialServicesTypes: [] as string[],
      reportingPeriodYear: new Date().getFullYear(),
    };
  },
  computed: {
    namesOfFilesToUpload(): string[] {
      const namesFromFilesToUpload = this.filesToUpload.map((el) => el.name.split(".")[0]);
      const namesFromUploadedFiles = this.uploadFiles.map((el) => el.name.split(".")[0]);
      return [...new Set([...namesFromFilesToUpload, ...namesFromUploadedFiles])];
    },
  },
  watch: {
    confirmedSelectedKPIs: function (newValue: { label: string; value: string }[]) {
      this.computedFinancialServicesTypes = newValue.map((el: { label: string; value: string }): string => {
        return this.euTaxonomyKPIsModel.companyTypeToEligibilityKpis[
          el.value as keyof typeof this.euTaxonomyKPIsModel.companyTypeToEligibilityKpis
        ];
      });
    },
    reportingPeriod: function (newValue: Date) {
      this.reportingPeriodYear = newValue.getFullYear();
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

    this.onThisPageLinks = [...this.onThisPageLinksStart];
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
      const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getEuTaxonomyDataForFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForFinancials(dataId);
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
      if (dataResponseData.data?.financialServicesTypes) {
        // types of company financial services
        const arrayWithCompanyKpiTypes = dataResponseData.data?.financialServicesTypes;
        // all types of financial services
        const allTypesOfFinancialServices = this.euTaxonomyKPIsModel.companyTypeToEligibilityKpis;

        this.selectedKPIs = this.kpisModel.filter((el: { label: string; value: string }) => {
          return arrayWithCompanyKpiTypes?.includes(
            allTypesOfFinancialServices[
              el.value as keyof typeof allTypesOfFinancialServices
            ] as EuTaxonomyDataForFinancialsFinancialServicesTypesEnum
          );
        });
        this.confirmeSelectedKPIs();
      }
      const receivedFormInputsModel = modifyObjectKeys(
        JSON.parse(JSON.stringify(dataResponseData)) as objectType,
        "receive"
      );
      this.waitingForData = false;

      await this.$nextTick();
      updateObject(this.formInputsModel, receivedFormInputsModel);
    },

    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;
        let allFileUploadedSuccessful = true;
        const documentUploadControllerControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentUploadController();
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();

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
          this.postEuTaxonomyDataForFinancialsResponse =
            await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
              formInputsModelToSend
            );
          console.log("Qqqqq", formInputsModelToSend);
        }
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
        this.confirmedSelectedKPIs = [];
        this.selectedKPIs = [];
        this.fiscalYearEnd = "";
        this.filesToUpload = [];
        this.uploadFiles = [];
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.UploadReports.clearAllNotUploadedFiles();
      }
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
     * Confirms the list of kpis to be generated
     *
     */
    confirmeSelectedKPIs() {
      this.confirmedSelectedKPIs = this.selectedKPIs;
      this.onThisPageLinks = [...new Set(this.onThisPageLinksStart.concat(this.selectedKPIs))];
    },

    /**
     * Deletes the specified kpis section
     *
     * @param value section name
     */
    removeKpisSection(value: string) {
      this.confirmedSelectedKPIs = this.confirmedSelectedKPIs.filter(
        (el: { label: string; value: string }) => el.value !== value
      );
      this.selectedKPIs = this.confirmedSelectedKPIs;
      this.onThisPageLinks = this.onThisPageLinks.filter((el: { label: string; value: string }) => el.value !== value);
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
     * Updates the Fiscal Year End value
     *
     * @param dateValue new date value
     */
    updateFiscalYearEndHandler(dateValue: Date) {
      this.convertedFiscalYearEnd = getHyphenatedDate(dateValue);
      this.fiscalYearEnd = dateValue;
    },
  },
});
</script>
