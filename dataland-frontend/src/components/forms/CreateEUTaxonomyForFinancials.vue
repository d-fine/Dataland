<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>Create EU Taxonomy Dataset for a Financial Company/Service</template>
    {{JSON.stringify(this.files.files)}}
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createEuTaxonomyForFinancialsForm"
            @submit="postEuTaxonomyDataForFinancials"
            #default="{ state: { valid } }"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :model-value="companyID"
              disabled="true"
            />
            <FormKit type="group" name="data" label="data">
              <div class="uploadFormSection grid">
                <div class="col-3 p-3 topicLabel">
                  <h4 id="general" class="anchor title">Upload company reports</h4>
                  <p>Please upload all relevant reports for this dataset in the PDF format.</p>
                </div>
                <!-- Select company reports -->
                <div class="col-9 formFields uploaded-files">
                  <h3 class="mt-0">Select company reports</h3>
                  <FileUpload
                    ref="fileUpload"
                    name="fileUpload"
                    accept=".pdf, .xlsx"
                    @select="onSelectedFiles"
                    :maxFileSize="maxFileSize"
                    invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
                    :fileLimit="1"
                    :auto="true"
                  >
                    <template #header="{ chooseCallback }">
                      <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
                        <div class="flex gap-2">
                          <PrimeButton
                            @click="chooseCallback()"
                            icon="pi pi-upload"
                            class="m-0"
                            label="UPLOAD REPORTS"
                          />
                        </div>
                      </div>
                    </template>
                    <template #content="{ uploadedFiles, removeUploadedFileCallback }">
                      <div v-if="uploadedFiles.length > 0">
                        <div
                          v-for="(file, index) of uploadedFiles"
                          :key="file.name + file.reportDate"
                          class="flex w-full align-items-center file-upload-item"
                        >
                          <span class="font-semibold flex-1">{{ file.name }}</span>
                          <div class="mx-2 text-black-alpha-50">{{ formatSize(file.size) }}</div>
                          <PrimeButton
                            icon="pi pi-times"
                            @click="onRemoveTemplatingFile(file, removeUploadedFileCallback, index)"
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
                      <FormKit :name="files.files[index].reportDate + file.name" type="group">
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
                              @update:modelValue="updateReportDate(index)"
                            />
                          </div>
                          {{ files.files[index].convertedReportDate }}

                          <FormKit
                            type="text"
                            v-model="files.files[index].convertedReportDate"
                            name="reportDate"
                            :outer-class="{ 'hidden-input': true }"
                          />
                        </div>

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
                    <h4 id="general" class="anchor title">Basic information</h4>
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
                          inputId="fiscalYearEnd"
                          v-model="fiscalYearEnd"
                          :showIcon="true"
                          dateFormat="D, M dd, yy"
                        />
                      </div>

                      <FormKit
                        type="text"
                        validation="required"
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
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Assurance</h4>
                  </div>

                  <!-- Level of assurance -->
                  <div class="col-9 formFields">
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
                        <div>
                          <UploadFormHeader
                              :name="euTaxonomyKpiNameMappings.tagName ?? ''"
                              :explanation="euTaxonomyKpiInfoMappings.tagName ?? ''"
                          />
                        <FormKit
                            outer-class="short"
                            type="text"
                            name="tagName"
                            placeholder="Tag Name"
                            validation-label="Tag Name"
                        />
                        </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Add KPIs</h4>
                    <p>Select at least one services type to add the related section of KPIs.</p>
                  </div>

                  <!-- Add KPIs -->
                  <div class="col-9 formFields">
                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader
                          :name="euTaxonomyKpiNameMappings.financialServicesTypes ?? ''"
                          :explanation="euTaxonomyKpiInfoMappings.financialServicesTypes ?? ''"
                      />

                      <MultiSelect
                        v-model="selectedKPIs"
                        :options="kpisModel"
                        name="financialServicesTypes"
                        optionLabel="label"
                        validation-label="Services Types"
                        validation="required"
                        placeholder="Select..."
                        class="mb-3"
                      />
                      <ul v-if="selectedKPIs.length">
                        <li :key="index" v-for="(file, index) of selectedKPIs">{{ file.label }}</li>
                      </ul>

                      <PrimeButton @click="confirmeSelectedKPIs" class="m-0" label="ADD RELATED KPIS" />
                    </div>
                  </div>
                </div>

                <FormKit name="eligibilityKpis" type="group">
                <div v-for="(copanyType, index) of confirmedSelectedKPIs" :key="copanyType" class="uploadFormSection">
                  <FormKit :name="copanyType.value" type="group">
                    <div class="flex w-full">
                      <h3>{{ copanyType.label }}</h3>

                      <PrimeButton
                        @click="removeKpisSection(copanyType.value)"
                        label="REMOVE THIS SECTION"
                        class="p-button-text ml-auto"
                        icon="pi pi-trash"
                      ></PrimeButton>
                    </div>

                    <div
                      v-for="kpiType of euTaxonomyKPIsModel[copanyType.value]"
                      :key="kpiType"
                      class="uploadFormSection"
                    >
                      <FormKit :name="kpiType" type="group">

                        <div class="col-9 formFields">
                          <div class="form-field">
                          <UploadFormHeader
                              :name="euTaxonomyKpiNameMappings[kpiType] ?? ''"
                              :explanation="euTaxonomyKpiInfoMappings[kpiType] ?? ''"
                          />
                              <KPIfieldsSet
                                  :KpiInfoMappings="euTaxonomyKpiInfoMappings"
                                  :KpiNameMappings="euTaxonomyKpiNameMappings"
                                  name="xxxxx" />
                          </div>
                        </div>
                      </FormKit>
                    </div>
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

              <!-- rest -->





            </FormKit>
          </FormKit>
          <template v-if="postEuTaxonomyDataForFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForFinancialsResponse"
              msg="EU Taxonomy Data"
              :message="postEuTaxonomyDataForFinancialsResponse.data"
              :messageId="messageCount"
            />
            <FailedUpload v-else msg="EU Taxonomy Data" :messageId="messageCount" />
          </template>
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
import KPIfieldsSet from "@/components/forms/parts/kpiSelection/KPIfieldsSet.vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { useFilesUploadedStore } from "@/stores/filesUploaded";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  euTaxonomyKPIsModel,
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/euTaxonomyKPIsModel.js";
import CheckBoxCustom from "@/components/forms/parts/CheckBoxCustom.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      fileUpload: ref<typeof FileUpload>(),
    };
  },
  name: "CreateEUTaxonomyForFinancials",
  components: {
    DataPointFormElement,
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
    KPIfieldsSet,
    CheckBoxCustom,
  },

  data: () => ({
    formInputsModel: {},
    files: useFilesUploadedStore(),
    fiscalYearEnd: "",
    convertedFiscalYearEnd: "",
    assuranceData: [
      { label: "None", value: "None" },
      { label: "LimitedAssurance", value: "LimitedAssurance" },
      { label: "ReasonableAssurance", value: "ReasonableAssurance" },
    ],
    maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
    euTaxonomyKPIsModel,
    euTaxonomyKpiNameMappings,
    euTaxonomyKpiInfoMappings,

    innerClass: {
      "formkit-inner": false,
      "p-inputwrapper": true,
    },
    inputClass: {
      "formkit-input": false,
      "p-inputtext": true,
      "w-full": true,
    },
    postEuTaxonomyDataForFinancialsProcessed: false,
    messageCount: 0,
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    kpisModel: [
      { label: "Asset Management", value: "AssetManagement" },
      { label: "Credit Insitution", value: "CreditInstitution" },
      { label: "Investment Firm", value: "InvestmentFirm" },
      { label: "Insurance & Re-insurance", value: "InsuranceOrReinsurance" },
    ],
    selectedKPIs: [],
    confirmedSelectedKPIs: [],
  }),
  watch: {
    fiscalYearEnd: function (newValue: Date) {
      if (newValue) {
        this.convertedFiscalYearEnd = this.converteDateFormat(newValue);
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
  methods: {
    converteDateFormat(date) {
      return `${date.getFullYear()}-${("0" + (date.getMonth() + 1).toString()).slice(-2)}-${(
        "0" + date.getDate().toString()
      ).slice(-2)}`;
    },
    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */


    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            this.formInputsModel
          );
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },

    onSelectedFiles(event) {

     // this.files.splice(index, 1, {...this.files[index], convertedReportDate: this.converteDateFormat(this.files[index].reportDate)});

      console.log("event", event);
      event.files[0]["reportDate"] = '';
      event.files[0]["convertedReportDate"] = '';
      this.files.setReportsFilesUploaded(event.files[0]);
      console.log("this.files", this.files.files);
    },

    onRemoveTemplatingFile(file, fileRemoveCallback: void, index: number) {
      fileRemoveCallback(index);
      this.files.removeReportFromFilesUploaded(file);
    },

    confirmeSelectedKPIs() {
      console.log("confirmedSelectedKPIs");
      this.confirmedSelectedKPIs = this.selectedKPIs;
    },

    removeKpisSection(value: string) {
      this.confirmedSelectedKPIs = this.confirmedSelectedKPIs.filter(
        (el: { label: string; value: string }) => el.value !== value
      );
      this.selectedKPIs = this.confirmedSelectedKPIs;
    },

    updateReportDate(index: number) {
      console.log('$forceUpdate');
      this.files.updatePropertyFilesUploaded(index, 'convertedReportDate', this.converteDateFormat(this.files.files[index].reportDate))
      this.files.reRender();
    },

    formatSize(bytes: number) {
      if (bytes === 0) {
        return "0 B";
      }
      const k = 1000,
        dm = 3,
        sizes = ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"],
        i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)).toString() + " " + sizes[i];
    },
  },
});
</script>
