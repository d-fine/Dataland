<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section">
      <FormKit :actions="false" type="form" @submit="submitRequest" @submit-invalid="handleInvalidInput" id="" name="">
        <div class="grid p-8 uploadFormWrapper">
          <div v-if="postBulkDataRequestObjectProcessed" class="col-12">
            <SuccessMessage :message="message" :messageId="messageCounter" />
            <FailMessage :message="message" :messageId="messageCounter" />
            <MessageComponent data-test="" severity="light-error">
              <template #text-info>Message:{{ message }}</template>
            </MessageComponent>
            <MessageComponent data-test="" severity="light-success">
              <template #text-info>Message:{{ message }}</template>
            </MessageComponent>
          </div>

          <div class="col-12">
            <div class="bg-white radius-1 p-4">
              <div class="grid">
                <div class="col-12">
                  <h4 class="p-0">Data Request Summary</h4>
                  <hr />
                </div>
                <div class="col-6">
                  <h4>3 Frameworks</h4>
                  <div class="paper-section radius-1 p-2 w-full selected-frameworks">
                    <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                      >No frameworks have been submitted.</span
                    >
                    <span class="form-list-item" :key="it" v-for="it in selectedFrameworks">
                      {{ it }}
                      <em @click="removeItem(it)" class="material-icons">close</em>
                    </span>
                  </div>
                </div>
                <div class="col-6">
                  <h4>11 Identifiers</h4>
                  <div class="paper-section radius-1 p-2 w-full selected-frameworks">
                    <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                      >No identifiers have been submitted.</span
                    >
                    <span class="form-list-item" :key="it" v-for="it in selectedFrameworks">
                      {{ it }}
                      <em @click="removeItem(it)" class="material-icons">close</em>
                    </span>
                  </div>
                </div>
                <div class="col-12 text-center">
                  <PrimeButton
                    type="submit"
                    label="Submit"
                    class="p-button p-button-outlined p-button-sm d-letters place-self-center ml-auto"
                    name="restart_data_button"
                  >
                    Restart Data Request
                  </PrimeButton>
                  <br />
                  <PrimeButton
                    type="submit"
                    label="Submit"
                    class="p-button p-button-text p-button-sm d-letters place-self-center ml-auto"
                    name="back_to_companies_button"
                  >
                    Back to Companies
                  </PrimeButton>
                </div>
              </div>
            </div>
          </div>

          <div class="col-12">
            <div class="grid">
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
                    optionValue="value"
                    optionLabel="label"
                    v-model:selectedItemsBindInternal="selectedFrameworks"
                    innerClass="long"
                  />
                  <FormKit
                    :modelValue="selectedFrameworks"
                    type="text"
                    validation="required"
                    validation-label="List of framework names"
                    :validation-messages="{
                      required: 'Select at least one framework',
                    }"
                    :outer-class="{ 'hidden-input': true }"
                  />
                  <h4 class="p-0">Added Frameworks:</h4>
                  <div class="paper-section radius-1 p-2 w-full selected-frameworks">
                    <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                      >No Frameworks added yet</span
                    >
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
                    validation-label="List of company identifiers"
                    :validation-messages="{
                      required: 'Provide at least one identifier',
                    }"
                    placeholder="Insert identifiers here. Separated by either comma, space, semicolon or linebreak."
                  />
                  <span class="gray-text font-italic"
                    >Accepted identifier types are: DUNS Number, LEI, ISIN & permID. Expected in comma separted
                    format.</span
                  >
                </div>
              </div>
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
import { type DataTypeEnum } from "@clients/backend";
import { type FrameworkSelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { ApiClientProvider } from "@/services/ApiClients";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { type BulkDataRequest } from "@clients/communitymanager";
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";

export default defineComponent({
  name: "RequestData",
  components: {
    MessageComponent,
    FailMessage,
    SuccessMessage,
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
      availableFrameworks: [] as { value: DataTypeEnum; label: string }[],
      selectedFrameworks: [] as Array<DataTypeEnum>,
      identifiersInString: "",
      identifiers: [] as Array<string>,
      messageCounter: 0,

      submittingSucceded: false,
      submittingInProgress: false,
      postBulkDataRequestObjectProcessed: true,
      message: "",
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
     * Builds a DataRequest object using the currently entered inputs and returns it
     * @returns the DataRequest object
     */
    collectDataToSend(): BulkDataRequest {
      return {
        listOfCompanyIdentifiers: this.identifiers,
        listOfFrameworkNames: this.selectedFrameworks,
      };
    },

    /**
     * Converts the string inside the input field into a list of identifiers
     */
    processInput() {
      const uniqueIdentifiers = new Set(this.identifiersInString.replace(/(\r\n|\n|\r|;| )/gm, ",").split(","));
      uniqueIdentifiers.delete("");
      this.identifiers = [...uniqueIdentifiers];
      console.log(this.identifiers);
    },

    handleInvalidInput() {
      console.log("IVALID", this.postBulkDataRequestObjectProcessed);
    },

    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      this.messageCounter++;
      this.processInput();
      try {
        this.submittingInProgress = true;
        const bulkDataRequestObject = this.collectDataToSend();
        const requestDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getRequestDataControllerApi();
        const response = await requestDataControllerApi.postBulkDataRequest(bulkDataRequestObject);
        console.log("response----------->", response);
        this.messageCounter++;
        this.message = response.message;
      } catch (error) {
        this.messageCounter++;
        console.error(error);
        this.message = error.message;
      } finally {
        this.submittingInProgress = false;
        this.postBulkDataRequestObjectProcessed = true;
      }
    },

    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveAvailableFrameworks() {
      this.availableFrameworks = ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
        return {
          value: dataTypeEnum,
          label: humanizeStringOrNumber(dataTypeEnum),
        };
      });
    },

    /**
     * Adds a new Object to the array
     */
    addItem() {
      this.idCounter++;
      this.listOfElementIds.push(this.idCounter);
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
  mounted() {
    void this.retrieveAvailableFrameworks();
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
