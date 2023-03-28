<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>Create EU Taxonomy Dataset for a Financial Company/Service</template>
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
                <h4 id="uploadReports" class="anchor title">Reporting Period</h4>
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
                            {{ formatSize(file.size) }}
                          </div>
                          <PrimeButton
                            icon="pi pi-times"
                            data-test="uploaded-files-remove"
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

                          <FormKit type="hidden" v-model="files.files[index].convertedReportDate" name="reportDate" />
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
                      :radioButtonsOptions="['Deviation', 'NoDeviation']"
                      required="required"
                    />

                    <!-- The date the fiscal year ends -->
                    <div class="form-field">
                      <UploadFormHeader
                        :name="euTaxonomyKpiNameMappings.fiscalYearEnd"
                        :explanation="euTaxonomyKpiInfoMappings.fiscalYearEnd"
                      />
                      <div class="lg:col-6 md:col-6 col-12 p-0">
                        <Calendar
                          data-test="fiscalYearEnd"
                          inputId="fiscalYearEnd"
                          v-model="fiscalYearEnd"
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

        <div id="jumpLinks" ref="jumpLinks" class="col-3 p-3 text-left jumpLinks">
          <h4 id="topicTitles" class="title">On this page</h4>
          <ul>
            <li v-for="(element, index) in onThisPageLinks" :key="index">
              <a @click="smoothScroll(`#${element.value}`)">{{ element.label }}</a>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";
import FileUpload from "primevue/fileupload";
import PrimeButton from "primevue/button";
import MultiSelect from "primevue/multiselect";
import KPIfieldSet from "@/components/forms/parts/kpiSelection/KPIfieldSet.vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
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
import { useFilesUploadedStore } from "@/stores/filesUploaded";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { smoothScroll } from "@/utils/smoothScroll";
import { checkCustomInputs } from "@/utils/validationsUtils";
import { getHyphenatedDate, formatSize } from "@/utils/DataFormatUtils";
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

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateEUTaxonomyForFinancials",
  components: {
    FailedUpload,
    FormKit,
    SuccessUpload,
    UploadFormHeader,
    Card,
    FileUpload,
    PrimeButton,
    Calendar,
    MultiSelect,
    YesNoComponent,
    KPIfieldSet,
  },

  data() {
    return {
      formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForFinancials,
      files: useFilesUploadedStore(),
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
      elementPosition: 0,
      scrollListener: (): null => null,
      smoothScroll,
      checkCustomInputs,
      formatSize,
      route: useRoute(),
      waitingForData: false,

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
  watch: {
    fiscalYearEnd: function (newValue: Date) {
      if (newValue) {
        this.convertedFiscalYearEnd = getHyphenatedDate(newValue);
      } else {
        this.convertedFiscalYearEnd = "";
      }
    },
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
      void this.loadEuData(dataId);
    }

    this.onThisPageLinks = [...this.onThisPageLinksStart];
    const jumpLinkselement = this.$refs.jumpLinks as HTMLElement;
    this.elementPosition = jumpLinkselement.getBoundingClientRect().top;
    this.scrollListener = (): null => {
      if (window.scrollY > this.elementPosition) {
        jumpLinkselement.style.position = "fixed";
        jumpLinkselement.style.top = "60px";
      } else {
        jumpLinkselement.style.position = "relative";
        jumpLinkselement.style.top = "0";
      }
      return null;
    };
    window.addEventListener("scroll", this.scrollListener);
  },
  unmounted() {
    window.removeEventListener("scroll", this.scrollListener);
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
        const formInputsModelToSend = modifyObjectKeys(
          JSON.parse(JSON.stringify(this.formInputsModel)) as objectType,
          "send"
        );
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            formInputsModelToSend
          );
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
        this.fiscalYearEnd = "";
        this.confirmedSelectedKPIs = [];
        this.selectedKPIs = [];
      }
    },

    /**
     * Modifies the file object and adds it to the store
     *
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
     */
    updateReportDateHandler(index: number) {
      this.files.updatePropertyFilesUploaded(
        index,
        "convertedReportDate",
        getHyphenatedDate(this.files.files[index].reportDate as unknown as Date)
      );
    },
  },
});
</script>
