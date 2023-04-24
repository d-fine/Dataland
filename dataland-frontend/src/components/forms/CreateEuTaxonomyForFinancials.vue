<template>
  <Card class="col-12 page-wrapper-card">
    <template #title
      ><span data-test="pageWrapperTitle"
        >{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Financial Company/Service</span
      ></template
    >
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
              :modelValue="companyID"
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

                <FormKit type="hidden" :modelValue="reportingPeriodYear" name="reportingPeriod" />
              </div>
              <FormKit type="group" name="data" label="data">
                <UploadReports
                  ref="UploadReports"
                  :filesToUpload="filesToUpload"
                  :listOfUploadedReportsInfo="listOfUploadedReportsInfo"
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :editMode="editMode"
                  @selectedFiles="onSelectedFilesHandler"
                  @removeReportFromFilesToUpload="removeReportFromFilesToUpload"
                  @removeReportFromUploadedReports="removeReportFromUploadedReports"
                  @updateReportDateHandler="updateReportDateHandler"
                />

                <BasicInformationFields
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :fiscalYearEndAsDate="fiscalYearEndAsDate"
                  :fiscalYearEnd="fiscalYearEnd"
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
                                :options="['None...', ...namesOfAllCompanyReportsForTheDataset]"
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
                        @click="confirmSelectedKPIs"
                        data-test="addKpisButton"
                        :label="selectedKPIs.length ? 'UPDATE KPIS' : 'ADD RELATED KPIS'"
                      />
                      <FormKit
                        :modelValue="computedFinancialServicesTypes"
                        type="text"
                        validationLabel="You must choose and confirm this "
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
                      v-for="kpiType of euTaxonomyPseudoModelAndMappings[copanyType.value]"
                      :key="kpiType"
                      :data-test="kpiType"
                      class="uploadFormSection"
                    >
                      <div class="col-9 formFields">
                        <FormKit :name="kpiType" type="group">
                          <div class="form-field">
                            <h3>
                              {{ euTaxonomyKpiNameMappings[kpiType] ?? "" }}
                            </h3>
                            <KPIfieldSet
                              :name="kpiType ?? ''"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :reportsName="namesOfAllCompanyReportsForTheDataset"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </div>
                  </FormKit>

                  <FormKit name="eligibilityKpis" type="group">
                    <FormKit
                      :name="euTaxonomyPseudoModelAndMappings?.companyTypeToEligibilityKpis[copanyType.value]"
                      type="group"
                    >
                      <div
                        v-for="kpiTypeEligibility of euTaxonomyPseudoModelAndMappings.eligibilityKpis"
                        :key="kpiTypeEligibility"
                        :data-test="kpiTypeEligibility"
                        class="uploadFormSection"
                      >
                        <div class="col-9 formFields">
                          <FormKit :name="kpiTypeEligibility" type="group">
                            <div class="form-field">
                              <h3>
                                {{ euTaxonomyKpiNameMappings[kpiTypeEligibility] ?? "" }}
                              </h3>
                              <KPIfieldSet
                                :name="kpiTypeEligibility ?? ''"
                                :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                                :kpiNameMappings="euTaxonomyKpiNameMappings"
                                :reportsName="namesOfAllCompanyReportsForTheDataset"
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
              v-if="postEuTaxonomyDataForFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailedUpload v-else :message="message" :messageId="messageCount" />
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
import { checkCustomInputs } from "@/utils/ValidationsUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyPseudoModelAndMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyPseudoModelAndMappings";
import {
  AssuranceDataAssuranceEnum,
  CompanyAssociatedDataEuTaxonomyDataForFinancials,
  DataMetaInformation,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from "@clients/backend";
import { AxiosError, AxiosResponse } from "axios";
import { modifyObjectKeys, ObjectType, updateObject } from "@/utils/UpdateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { ExtendedCompanyReport, ExtendedFile, WhichSetOfFiles } from "@/components/forms/Types";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";
import {
  completeInformationAboutSelectedFileWithAdditionalFields,
  updatePropertyFilesUploaded,
} from "@/utils/EuTaxonomyUtils";
import { calculateSha256HashFromFile } from "@/utils/GenericUtils";
import { DocumentUploadResponse } from "@clients/documentmanager";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateEuTaxonomyForFinancials",
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
  emits: ["datasetCreated"],
  data() {
    return {
      formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForFinancials,
      filesToUpload: [] as ExtendedFile[],
      listOfUploadedReportsInfo: [] as ExtendedCompanyReport[],
      fiscalYearEndAsDate: null as Date | null,
      fiscalYearEnd: "",
      reportingPeriod: new Date(),
      assuranceData: {
        None: humanizeString(AssuranceDataAssuranceEnum.None),
        LimitedAssurance: humanizeString(AssuranceDataAssuranceEnum.LimitedAssurance),
        ReasonableAssurance: humanizeString(AssuranceDataAssuranceEnum.ReasonableAssurance),
      },
      euTaxonomyPseudoModelAndMappings,
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      checkCustomInputs,
      formatBytesUserFriendly,
      updatePropertyFilesUploaded,
      route: useRoute(),
      waitingForData: false,
      editMode: false,

      postEuTaxonomyDataForFinancialsProcessed: false,
      messageCount: 0,
      postEuTaxonomyDataForFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
      uploadFileResponse: null as AxiosResponse<DocumentUploadResponse> | null,
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
      message: "",
    };
  },
  computed: {
    namesOfAllCompanyReportsForTheDataset(): string[] {
      const namesFromFilesToUpload = this.filesToUpload.map((el) => el.name.split(".")[0]);
      const namesFromListOfUploadedReports = this.listOfUploadedReportsInfo.map((el) => el.name.split(".")[0]);
      return [...new Set([...namesFromFilesToUpload, ...namesFromListOfUploadedReports])];
    },
    reportingPeriodYear(): number {
      return this.reportingPeriod.getFullYear();
    },
  },
  watch: {
    confirmedSelectedKPIs: function (newValue: { label: string; value: string }[]) {
      this.computedFinancialServicesTypes = newValue.map((el: { label: string; value: string }): string => {
        return euTaxonomyPseudoModelAndMappings.companyTypeToEligibilityKpis[
          el.value as keyof typeof euTaxonomyPseudoModelAndMappings.companyTypeToEligibilityKpis
        ];
      });
    },
  },

  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  mounted() {
    const dataId = this.route.query.templateDataId;
    if (typeof dataId === "string" && dataId !== "") {
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
      if (dataResponseData?.reportingPeriod) {
        this.reportingPeriod = new Date(dataResponseData.reportingPeriod);
      }
      if (dataResponseData.data?.fiscalYearEnd) {
        this.fiscalYearEndAsDate = new Date(dataResponseData.data.fiscalYearEnd);
      }
      if (dataResponseData.data?.referencedReports) {
        const referencedReportsForDataId = dataResponseData.data.referencedReports;
        for (const key in referencedReportsForDataId) {
          this.listOfUploadedReportsInfo.push({
            name: key,
            currency: referencedReportsForDataId[key].currency,
            isGroupLevel: referencedReportsForDataId[key].isGroupLevel,
            reference: referencedReportsForDataId[key].reference,
            reportDate: referencedReportsForDataId[key].reportDate,
            reportDateAsDate: referencedReportsForDataId[key].reportDate
              ? new Date(referencedReportsForDataId[key].reportDate as string)
              : "",
          });
        }
      }
      if (dataResponseData.data?.financialServicesTypes) {
        // types of company financial services
        const arrayWithCompanyKpiTypes = dataResponseData.data?.financialServicesTypes;
        // all types of financial services
        const allTypesOfFinancialServices = euTaxonomyPseudoModelAndMappings.companyTypeToEligibilityKpis;

        this.selectedKPIs = this.kpisModel.filter((el: { label: string; value: string }) => {
          return arrayWithCompanyKpiTypes?.includes(
            allTypesOfFinancialServices[
              el.value as keyof typeof allTypesOfFinancialServices
            ] as EuTaxonomyDataForFinancialsFinancialServicesTypesEnum
          );
        });
        this.confirmSelectedKPIs();
      }
      const receivedFormInputsModel = modifyObjectKeys(
        JSON.parse(JSON.stringify(dataResponseData)) as ObjectType,
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
        ).getDocumentControllerApi();
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();

        if (this.filesToUpload.length) {
          for (let index = 0; index < this.filesToUpload.length; index++) {
            try {
              const hash = await calculateSha256HashFromFile(this.filesToUpload[index]);
              const documentExists = await documentUploadControllerControllerApi.checkDocument(hash);
              if (!documentExists.data.documentExists) {
                this.uploadFileResponse = await documentUploadControllerControllerApi.postDocument(
                  this.filesToUpload[index]
                );
                this.filesToUpload[index]["documentId"] = this.uploadFileResponse.data.documentId;
              } else {
                this.filesToUpload[index]["documentId"] = hash;
              }
            } catch (error) {
              this.messageCount++;
              console.error(error);
              if (error instanceof AxiosError) {
                this.message =
                  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
                  (error.response?.data.errors[0]?.summary as string) +
                  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
                  (error.response?.data.errors[0]?.message as string); //TODO: fix types
              } else {
                this.message =
                  "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
              }
              allFileUploadedSuccessful = false;
              break;
            }
          }
        }
        if (allFileUploadedSuccessful) {
          await this.$nextTick();
          const formInputsModelToSend = modifyObjectKeys(
            JSON.parse(JSON.stringify(this.formInputsModel)) as ObjectType,
            "send"
          );
          this.postEuTaxonomyDataForFinancialsResponse =
            await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
              formInputsModelToSend
            );
        }
      } catch (error) {
        this.messageCount++;
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = "An error occurred: " + error.message;
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        }
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
        this.confirmedSelectedKPIs = [];
        this.selectedKPIs = [];
        this.fiscalYearEndAsDate = null;
        this.filesToUpload = [];
        this.listOfUploadedReportsInfo = [];
        this.formInputsModel = {};
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.UploadReports.clearAllSelectedFiles();
        await this.$nextTick();
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
        this.$emit("datasetCreated");
      }
    },

    /**
     * Add files to object filesToUpload
     *
     * @param event full event object containing the files
     * @param event.originalEvent event information
     * @param event.files files
     */
    onSelectedFilesHandler(event: { files: Record<string, string>[]; originalEvent: Event }): void {
      if (event.files.length) {
        this.filesToUpload = [
          ...completeInformationAboutSelectedFileWithAdditionalFields(event.files, this.listOfUploadedReportsInfo),
        ] as ExtendedFile[];
      } else {
        return;
      }
    },

    /**
     * Removes a report from the list of files to be uploaded
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
     * Removes a report from the list of already uploaded reports while the user edits a dataset. That way it is no
     * longer included as referenced report after the edit it submitted.
     *
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromUploadedReports(indexOfFileToRemove: number) {
      this.listOfUploadedReportsInfo.splice(indexOfFileToRemove, 1);
    },

    /**
     * Confirms the list of kpis to be generated
     *
     */
    confirmSelectedKPIs() {
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
      const updatedSetOfFiles = this.updatePropertyFilesUploaded(
        index,
        "reportDateAsDate",
        dateValue,
        this[whichSetOfFiles]
      );
      this[whichSetOfFiles] = [...updatedSetOfFiles];
    },

    /**
     * Updates the Fiscal Year End value
     *
     * @param dateValue new date value
     */
    updateFiscalYearEndHandler(dateValue: Date) {
      this.fiscalYearEnd = getHyphenatedDate(dateValue);
      this.fiscalYearEndAsDate = dateValue;
    },
  },
});
</script>
