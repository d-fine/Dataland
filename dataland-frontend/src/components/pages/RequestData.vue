<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section">
      <FormKit :actions="false" type="form"
               @submit="submitRequest"
               @submit-invalid="handleInvalidInput" id="" name="">
        <div class="grid p-8 uploadFormWrapper">
          <div class="col-12 next-to-each-other">
            <h2>Request Data</h2>
            <PrimeButton
              type="submit"
              label="Submit"
              class="p-button p-button-sm d-letters place-self-center ml-auto"
              name="submit_request_button"
            >
              Submit Data Request
            </PrimeButton>
          </div>
          <div class="col-6">
            <div class="bg-white radius-1 p-4">
              <h4 class="p-0">Please select the framework(s) for which you want to request data:</h4>
              <MultiSelectFormFieldBindData
                label="Frameworks"
                placeholder="Select framework"
                description="Select the frameworks you would like data for"
                name="listOfFrameworkNames"
                :options="availableFrameworks"
                optionValue="frameworkDataType"
                optionLabel="displayName"
                v-model:selectedItemsBindInternal="selectedFrameworks"
                innerClass="long"
              />
              <h4 class="p-0">Added Frameworks:</h4>
              <div class="paper-section radius-1 p-2 w-full selected-frameworks">
                <span v-if="!selectedFrameworks.length" class="gray-text no-framework">No Frameworks added yet</span>
                <span class="form-list-item" :key="it" v-for="it in selectedFrameworks">
                  {{ it }}
                  <em @click="removeItem(it)" class="material-icons">close</em>
                </span>
              </div>
            </div>
          </div>
          <div class="col-6">
            <div class="bg-white radius-1 p-4">
              <h4 class="p-0">Provide Company Identifiers</h4>
              <FormKit
                v-model="identifiersInString"
                type="textarea"
                name="listOfCompanyIdentifiers"
                validation="required"
                placeholder="Insert identifiers here. Separated by either comma, space, semicolon or linebreak."
              />
              <span class="gray-text font-italic"
                >Accepted identifier types are: DUNS Number, LEI, ISIN & permID. Expected in comma separted
                format.</span
              >
            </div>
          </div>
        </div>
      </FormKit>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import {CompanyAssociatedDataEuTaxonomyDataForFinancials, type DataTypeEnum} from "@clients/backend";
import { type FrameworkSelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
import {ObjectType} from "@/utils/UpdateObjectUtils";
import {checkIfAllUploadedReportsAreReferencedInDataModel} from "@/utils/ValidationsUtils";
import {DocumentToUpload, uploadFiles} from "@/utils/FileUploadUtils";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {ApiClientProvider} from "@/services/ApiClients";
import {formatAxiosErrorMessage} from "@/utils/AxiosErrorMessageFormatter";

export default defineComponent({
  name: "RequestData",
  components: {
    MultiSelectFormFieldBindData,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    TheFooter,
    PrimeButton,
    FormKit,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data() {
    return {
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<DataTypeEnum>,
      identifiersInString: "",
      identifiers: [] as Array<string>,

      // submissionFinished: false, TODO
      // submissionInProgress: false, TODO
      //isFormFilledCorrect: false, TODO will adjust based on if the form is filled correctly (similar to upload page)
    };
  },

  computed: {
    selectedFrameworksInt: {
      get(): Array<FrameworkSelectableItem> {
        return this.availableFrameworks.filter((frameworkSelectableItem) =>
          this.selectedFrameworks.includes(frameworkSelectableItem.frameworkDataType),
        );
      },
      set() {
        console.log("TODO");
      },
    },

    /*submissionProgressTitle() {
      if (this.submissionFinished) {
        if (this.isInviteSuccessful) {
          return "Success";
        } else {
          return "Submission failed";
        }
      } else {
        return "Submitting file";
      }
    },*/
  },

  methods: {
    /**
     * Remove framework from selected frameworks from array
     * @param it - framework to remove
     */
    removeItem(it: string) {
      this.selectedFrameworks = this.selectedFrameworks.filter((el) => el !== it);
    },

    /**
     * Converts the string inside the input field into a list of identifiers
     */
    processInput() {
      console.log(this.input);
      const uniqueIdentifiers = new Set(this.input.replace(/(\r\n|\n|\r|;| )/gm, ",").split(","));
      uniqueIdentifiers.delete("");
      this.identifiers = [...uniqueIdentifiers];
      console.log(this.identifiers);
    },

    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;

        // JSON.parse/stringify used to clone the formInputsModel in order to stop Proxy refreneces
        const clonedFormInputsModel = JSON.parse(JSON.stringify(this.formInputsModel)) as ObjectType;
        const kpiSections = (clonedFormInputsModel.data as ObjectType).kpiSections;
        delete (clonedFormInputsModel.data as ObjectType).kpiSections;
        clonedFormInputsModel.data = {
          ...(clonedFormInputsModel.data as ObjectType),
          ...this.convertKpis(kpiSections as ObjectType),
        };

        checkIfAllUploadedReportsAreReferencedInDataModel(
            this.formInputsModel.data as ObjectType,
            Object.keys(this.namesAndReferencesOfAllCompanyReportsForTheDataset),
        );

        await uploadFiles(
            (this.$refs.UploadReports.$data as { documentsToUpload: DocumentToUpload[] }).documentsToUpload,
            assertDefined(this.getKeycloakPromise),
        );

        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)(),
        ).getUnifiedFrameworkDataController(DataTypeEnum.EutaxonomyFinancials);
        this.postEuTaxonomyDataForFinancialsResponse = await euTaxonomyDataForFinancialsControllerApi.postFrameworkData(
            clonedFormInputsModel as CompanyAssociatedDataEuTaxonomyDataForFinancials,
        );
        this.$emit("datasetCreated");
      } catch (error) {
        this.messageCount++;
        console.error(error);
        this.message = formatAxiosErrorMessage(error as Error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },

    handleInvalidInput() {
      alert('IVALID')
    },

    /**
     * Submits the data request to the request service
     */
    submitRequest(): Promise<void> {
      this.processInput();
    },

    /**
     * Resets all the filters to their default values (i.e. select all frameworks but no countries / sectors)
     */
    /*resetFrameworkFilter() {
      this.selectedFrameworksInt = this.availableFrameworks.filter((it) => !it.disabled);
    },*/

    /**
     * Refreshes the page to allow the user to make a new data request
     */
    /*createNewRequest() {
      this.$router.go();
    },*/

    /**
     * Called when the user hits submit. Enables the progress bar and uploads the file.
     */
    /*async handleSubmission() {
      this.submissionInProgress = true;
    // TODO: some POST request is sent
      this.submissionFinished = true;
      this.submissionInProgress = false;
    },*/

    /**
     * Updates the UI to reflect the result of the file upload
     * @param response the result of the file upload request
     */
    /*readInviteStatusFromResponse(response: AxiosResponse<InviteMetaInfoEntity>) {
      this.isInviteSuccessful = response.data.wasInviteSuccessful ?? false;
      this.inviteResultMessage = response.data.inviteResultMessage ?? "No response from server.";
    },*/
  },
});
</script>

<style scoped>
.selected-frameworks {
  min-height: 100px;
}
.no-framework {
  display: flex;
  justify-content: center;
  height: 100px;
  align-items: center;
}
</style>
