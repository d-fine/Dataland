<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section no-ui-message">
      <FormKit :actions="false" type="form" @submit="submitRequest" id="requestDataFormId" name="requestDataFormName">
        <div class="grid p-8 uploadFormWrapper">
          <div class="col-12" v-if="postBulkDataRequestObjectProcessed">
            <div data-test="submittingSuccededMessage" v-if="submittingSucceded">
              <MessageComponent
                v-if="acceptedCompanyIdentifiers.length"
                data-test="someIdentifiersPassed"
                severity="light-success"
              >
                <template #left-icon>
                  <em class="material-icons info-icon p-message-icon green-text">check_circle</em>
                </template>
                <template #text-info>
                  <h4>Data request submitted succesfully.</h4>
                  <p class="fw-semi-bold" v-if="!rejectedCompanyIdentifiers.length">
                    All identifiers have been submitted successfully.
                  </p>
                  <p class="fw-semi-bold red-text" v-if="rejectedCompanyIdentifiers.length">
                    However, some identifiers couldn’t be recognised.
                  </p>
                </template>
              </MessageComponent>
              <MessageComponent v-else data-test="nonIdentifiersPassed" severity="light-error">
                <template #left-icon>
                  <em class="material-icons info-icon p-message-icon red-text">error</em>
                </template>
                <template #text-info>
                  <h4>Data request couldn’t be submitted.</h4>
                  <p class="fw-semi-bold">
                    Check the format of the identifiers and try again. Accepted identifiers are: LEI, ISIN & permID.
                    Expected in comma, semicolon, linebreaks and spaces separted format.
                  </p>
                </template>
              </MessageComponent>
            </div>
            <FailMessage
              data-test="failMessage"
              v-else
              :message="message"
              :summary="summary"
              :messageId="messageCounter"
            />
          </div>

          <div class="col-12" v-if="submittingSucceded">
            <div data-test="nonIdentifiersPassed" class="bg-white radius-1 p-4">
              <div class="grid">
                <div class="col-12">
                  <h4 class="p-0">Data Request Summary</h4>
                  <hr />
                </div>
                <div class="col-4">
                  <div class="next-to-each-other align-items-center">
                    <em class="material-icons info-icon green-text">check_circle</em>
                    <h4>{{ selectedFrameworks.length ?? 0 }} Frameworks</h4>
                  </div>
                  <div class="paper-section radius-1 p-2 w-full selected-frameworks">
                    <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                      >No frameworks have been submitted.</span
                    >
                    <p class="m-1" v-else v-for="it in humanizedSelectedFrameworks" :key="it">
                      {{ it }}
                    </p>
                  </div>
                </div>
                <div class="col-4">
                  <div class="next-to-each-other align-items-center">
                    <em class="material-icons info-icon green-text">check_circle</em>
                    <h4>{{ acceptedCompanyIdentifiers.length ?? 0 }} Accepted Company Identifiers</h4>
                  </div>
                  <div
                    class="paper-section radius-1 p-2 w-full selected-frameworks"
                    data-test="acceptedCompanyIdentifiers"
                  >
                    <span v-if="!acceptedCompanyIdentifiers.length" class="gray-text no-framework"
                      >No accepted identifiers have been submitted.</span
                    >
                    <span data-test="identifier" v-for="it in acceptedCompanyIdentifiers" :key="it"> {{ it }}, </span>
                  </div>
                </div>
                <div class="col-4">
                  <div class="next-to-each-other align-items-center">
                    <em class="material-icons info-icon red-text">error</em>
                    <h4 :class="rejectedCompanyIdentifiers.length ? 'red-text' : null">
                      {{ rejectedCompanyIdentifiers.length ?? 0 }} Rejected Company Identifiers
                    </h4>
                  </div>
                  <div
                    class="paper-section radius-1 p-2 w-full selected-frameworks"
                    :class="rejectedCompanyIdentifiers.length ? 'red-border' : null"
                    data-test="rejectedCompanyIdentifiers"
                  >
                    <span v-if="!rejectedCompanyIdentifiers.length" class="gray-text no-framework"
                      >No rejected identifiers.</span
                    >
                    <span data-test="identifier" v-for="it in rejectedCompanyIdentifiers" :key="it"> {{ it }}, </span>
                  </div>
                </div>
                <div class="col-12 text-center">
                  <PrimeButton
                    @click="resetForm"
                    class="p-button p-button-outlined p-button-sm d-letters place-self-center ml-auto"
                    name="restart_data_button"
                    data-test="resetFormButton"
                  >
                    Restart Data Request
                  </PrimeButton>
                  <br />
                  <PrimeButton
                    @click="goToCompanies"
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

          <div class="col-12" v-else>
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
                <div data-test="selectFrameworkDiv" class="bg-white radius-1 p-4">
                  <h4 class="p-0">Select at least one framework</h4>
                  <MultiSelectFormFieldBindData
                    data-test="selectFrameworkSelect"
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
                  <div data-test="addedFrameworks" class="paper-section radius-1 p-2 w-full selected-frameworks">
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
                <div data-test="provideIdentifiers" class="bg-white radius-1 p-4">
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
                    >Accepted identifier types are: LEI, ISIN & permID. Expected in comma, semicolon, linebreaks and
                    spaces separted format.</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </FormKit>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import { type DataTypeEnum, type ErrorResponse } from "@clients/backend";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheFooter from "@/components/generics/TheNewFooter.vue";
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { type BulkDataRequest } from "@clients/communitymanager";
import FailMessage from "@/components/messages/FailMessage.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import { AxiosError } from "axios";

export default defineComponent({
  name: "RequestData",
  components: {
    MessageComponent,
    FailMessage,
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === "/");
    const footerContent = footerPage?.sections;
    return {
      availableFrameworks: [] as { value: DataTypeEnum; label: string }[],
      selectedFrameworks: [] as Array<DataTypeEnum>,
      identifiersInString: "",
      identifiers: [] as Array<string>,
      messageCounter: 0,
      acceptedCompanyIdentifiers: [] as Array<string>,
      rejectedCompanyIdentifiers: [] as Array<string>,
      submittingSucceded: false,
      submittingInProgress: false,
      postBulkDataRequestObjectProcessed: false,
      message: "",
      summary: "",
      footerContent,
    };
  },

  computed: {
    humanizedSelectedFrameworks(): string[] {
      return this.selectedFrameworks.map((it) => humanizeStringOrNumber(it));
    },
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
     * Converts the string inside the identifiers field into a list of identifiers
     */
    processInput() {
      const uniqueIdentifiers = new Set(this.identifiersInString.replace(/(\r\n|\n|\r|;| )/gm, ",").split(","));
      uniqueIdentifiers.delete("");
      this.identifiers = [...uniqueIdentifiers];
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
        const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        const response = await requestDataControllerApi.postBulkDataRequest(bulkDataRequestObject);

        this.messageCounter++;
        this.message = response.data.message;
        this.rejectedCompanyIdentifiers = response.data.rejectedCompanyIdentifiers;
        this.acceptedCompanyIdentifiers = response.data.acceptedCompanyIdentifiers;
        this.submittingSucceded = true;
      } catch (error) {
        this.messageCounter++;
        console.error(error);
        if (error instanceof AxiosError) {
          const responseMessages = (error.response?.data as ErrorResponse)?.errors;
          this.message = responseMessages ? responseMessages[0].message : error.message;
          this.summary = responseMessages[0].summary;
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        }
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
     * Resets form to allow the user to make a new data request
     */
    resetForm() {
      this.acceptedCompanyIdentifiers = [];
      this.rejectedCompanyIdentifiers = [];
      this.selectedFrameworks = [];
      this.identifiersInString = "";
      this.identifiers = [];
      this.postBulkDataRequestObjectProcessed = false;
      this.submittingSucceded = false;
    },

    /**
     * Go to companies page
     */
    goToCompanies() {
      void this.$router.push("/companies");
    },
  },
  mounted() {
    this.retrieveAvailableFrameworks();
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
