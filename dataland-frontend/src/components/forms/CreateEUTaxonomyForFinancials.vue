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
                    name="demo[]"
                    url="./upload"
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
                            :disabled="this.files.length"
                          />
                        </div>
                      </div>
                    </template>
                    <template #content="{ uploadedFiles, removeUploadedFileCallback }">
                      <div v-if="uploadedFiles.length > 0">
                        <div
                          v-for="(file, index) of uploadedFiles"
                          :key="file.name + file.type + file.size"
                          class="flex w-full align-items-center file-upload-item"
                        >
                          <span class="font-semibold flex-1">{{ file.name }}</span>
                          <div class="mx-2 text-black-alpha-50">{{ formatSize(file.size) }}</div>
                          <Badge value="Completed" class="mt-3" severity="success" />
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
                <!-- Select company reports -->
                <div v-if="files[0]?.name" class="col-9 formFields">
                  <div class="form-field-label">
                    <h3 class="mt-0">{{ files[0].name }}</h3>

                    <PrimeButton
                        @click="onRemoveTemplatingFile(files[0], removeUploadedFileCallback, 0)"
                        label="REMOVED"
                        class="p-button-text"
                        icon="pi pi-trash"
                    ></PrimeButton>
                  </div>

                  <!-- Date of the report -->
                  <div class="form-field">
                    <UploadFormHeader
                        name="Date of the report"
                        explanation="Date of the report"
                    />
                    <div class="lg:col-6 md:col-6 col-12 p-0">
                      <Calendar
                        data-test="dateOfTheReport"
                        inputId="icon"
                        :showIcon="true"
                        dateFormat="D, M dd, yy"
                        :maxDate="new Date()"
                      />
                    </div>

                    <FormKit
                      type="text"
                      validation="required"
                      name="dateOfTheReport"
                      :outer-class="{ 'hidden-input': true }"
                    />
                  </div>

                  <!-- Currency used in the report -->
                  <div class="form-field" data-test="currencyUsedInTheReport">
                    <UploadFormHeader
                        name="Currency used in the report"
                        explanation="Currency used in the report"
                    />
                    <div class="lg:col-6 md:col-6 col-12 p-0">
                    <FormKit
                        type="text"
                        name="currencyUsedInTheReport"
                        validation="required"
                        validation-label="Currency used in the report"
                        placeholder="Currency used in the report"
                    />
                    </div>

                  </div>
                  <div class="form-field">
                    <CheckBoxCustom name="integratedReportIsOnAGroupLevel" explanation="Integrated report is on a group level" />
                  </div>

                </div>
                </div>

                <div class="uploadFormSection">

                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Basic information</h4>
                  </div>
                  <!-- Basic information -->
                  <div class="col-9 formFields">
                    <h3 class="mt-0">Basic information</h3>
                    <CheckBoxCustom name="fiscalYearIsDeviating" explanation="Fiscal year is deviating" />

                    <!-- The date the fiscal year ends -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="The date the fiscal year ends"
                          explanation="The date the fiscal year ends"
                      />
                      <div class="lg:col-6 md:col-6 col-12 p-0">
                        <Calendar
                            inputId="icon"
                            :showIcon="true"
                            dateFormat="D, M dd, yy"
                            :maxDate="new Date()"
                        />
                      </div>

                      <FormKit
                          type="text"
                          validation="required"
                          name="dateTheFiscalYearEnds"
                          :outer-class="{ 'hidden-input': true }"
                      />
                    </div>

                    <!-- Scope of entities -->
                    <div class="form-field">
                      <CheckBoxCustom name="scopeOfEntities *" explanation="Scope of entities *" />
                    </div>

                    <!-- EU Taxonomy activity level reporting -->
                    <div class="form-field">
                      <CheckBoxCustom name="euTaxonomyActivityLevelReporting" explanation="EU Taxonomy activity level reporting *" />
                    </div>

                    <!-- Number of employees -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Number of employees"
                          explanation="Number of employees"
                      />
                      <FormKit
                          type="number"
                          name="numberOfEmployees"
                          validation-label="Number of employees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                          :inner-class="{ short: true }"
                      />
                    </div>

                  </div>
                </div>


                  <div class="uploadFormSection">

                    <div class="col-3 p-3 topicLabel">
                      <h4 id="general" class="anchor title">NFRD & Assurance</h4>
                    </div>
                    <!-- NFRD & Assurance -->
                  <div class="col-9 formFields">
                    <h3 class="mt-0">NFRD</h3>

                    <!-- Scope of entities -->
                    <div class="form-field">
                      <CheckBoxCustom name="mandatoryNFRD" explanation="NFRD is mandatory *" />
                    </div>

                    <!-- The date the fiscal year ends -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="The date the fiscal year ends"
                          explanation="The date the fiscal year ends"
                      />
                      <div class="lg:col-6 md:col-6 col-12 p-0">
                        <Calendar
                            inputId="icon"
                            :showIcon="true"
                            dateFormat="D, M dd, yy"
                            :maxDate="new Date()"
                        />
                      </div>

                      <FormKit
                          type="text"
                          validation="required"
                          name="dateTheFiscalYearEnds"
                          :outer-class="{ 'hidden-input': true }"
                      />
                    </div>

                    <!-- Scope of entities -->
                    <div class="form-field">
                      <CheckBoxCustom name="scopeOfEntities *" explanation="Scope of entities *" />
                    </div>

                    <!-- EU Taxonomy activity level reporting -->
                    <div class="form-field">
                      <CheckBoxCustom name="euTaxonomyActivityLevelReporting" explanation="EU Taxonomy activity level reporting *" />
                    </div>

                    <!-- Number of employees -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Number of employees"
                          explanation="Number of employees"
                      />
                      <div class="lg:col-4 md:col-6 col-12 p-0">
                      <FormKit
                          type="number"
                          name="numberOfEmployees"
                          validation-label="Number of employees"
                          placeholder="Number"
                          validation="required|number"
                          step="1"
                          min="0"
                      />
                      </div>
                    </div>

                  </div>



                  <!-- Level of assurance -->
                  <div class="col-9 formFields">
                    <h3 class="mt-0">Level of assurance</h3>

                    <!-- Level of assurance -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Level of assurance *"
                          explanation="Level of assurance"
                      />
                      <div class="lg:col-4 md:col-6 col-12 p-0">
                      <FormKit
                          type="select"
                          name="levelOfAssurance"
                          placeholder="None"
                          validation-label="Level of assurance"
                          validation="required"
                      />
                      </div>
                    </div>

                    <!-- Assurance provider -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Assurance provider *"
                          explanation="Assurance provider"
                      />
                      <FormKit
                          type="text"
                          name="assuranceProvider"
                          placeholder="Assurance provider"
                          validation-label="Assurance provider"
                          validation="required"
                      />
                    </div>

                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Data source *"
                          explanation="Data source"
                      />
                      <div class="next-to-each-other">
                        <FormKit
                            outer-class="flex-1"
                            type="select"
                            name="page"
                            placeholder="Select a report"
                            validation-label="Select a report"
                            validation="required"
                            :options="files"
                        />
                        <FormKit
                            outer-class="w-100"
                            type="number"
                            name="page"
                            placeholder="Page"
                            validation-label="Page"
                            validation="required"
                        />
                      </div>
                    </div>

                  </div>

                  </div>

                <div class="uploadFormSection">

                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Add KPIs</h4>
                    <p>Select at least one company type to add the related section of KPIs.</p>
                  </div>

                  <!-- Add KPIs -->
                  <div class="col-9 formFields">

                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader
                          name="Company type *"
                          explanation="Company type *"
                      />
                      <FormKit
                          type="select"
                          name="companyType"
                          validation-label="Company type"
                          validation="required"
                          :options="files"
                      />


                      <PrimeButton
                          @click=""
                          class="m-0"
                          label="ADD RELATED KPIS"
                          :disabled="valid"
                      />

                    </div>

                  </div>

                </div>



                <div class="form-field">
                </div>


              </div>




              <!-- rest -->
              <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                <template
                  v-for="fsType in ['CreditInstitution', 'InsuranceOrReinsurance', 'AssetManagement', 'InvestmentFirm']"
                  :key="fsType"
                >
                  <div :name="fsType">
                    <FormKit type="group" :name="fsType">
                      <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                      <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                      <DataPointFormElement name="taxonomyNonEligibleActivity" label="Taxonomy Non Eligible Activity" />
                      <DataPointFormElement name="derivatives" label="Derivatives" />
                      <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                      <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                    </FormKit>
                  </div>
                </template>
              </FormKit>
              <FormKit type="group" name="creditInstitutionKpis" label="Credit Institution KPIs">
                <h4>Credit Institution KPIs</h4>
                <div name="creditInstitutionKpis">
                  <DataPointFormElement name="tradingPortfolio" label="Trading Portfolio" />
                  <DataPointFormElement name="interbankLoans" label="Interbank Loans" />
                  <DataPointFormElement
                    name="tradingPortfolioAndInterbankLoans"
                    label="Trading Portfolio and Interbank Loans (combined)"
                  />
                  <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
                </div>
              </FormKit>
              <FormKit type="group" name="insuranceKpis" label="Insurance KPIs">
                <h4>Insurance KPIs</h4>
                <DataPointFormElement
                  name="taxonomyEligibleNonLifeInsuranceActivities"
                  label="Taxonomy Eligible non Life Insurance Activities"
                />
              </FormKit>
              <FormKit type="group" name="investmentFirmKpis" label="Investment Firm KPIs">
                <h4>Investment Firm KPIs</h4>
                <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
              </FormKit>
              <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
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
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import CheckBoxCustom from "@/components/forms/parts/CheckBoxCustom.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
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
    CheckBoxCustom,
  },

  data: () => ({
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
    maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
    messageCount: 0,
    formInputsModel: {},
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    files: [],
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
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
      console.log("event", event);
      this.files = event.files;
    },

    onRemoveTemplatingFile(file, fileRemoveCallback: void, index: number) {
      fileRemoveCallback(index);
      this.files = this.files.filter( (el) => {
        return el.name !== file.name;
      })

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
