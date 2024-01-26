<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section no-ui-message">
      <div class="col-12 mb-2 bg-white">
        <div class="text-left company-details px-4">
          <h1 data-test="headerLabel">Bulk Data Request</h1>
        </div>
      </div>

      <div class="col-12">
        <FormKit
          :actions="false"
          v-model="bulkDataRequestModel"
          type="form"
          @submit="submitRequest"
          id="requestDataFormId"
          name="requestDataFormName"
        >
          <div class="grid px-8 py-4 justify-content-center uploadFormWrapper">
            <div class="col-12 md:col-6 xl:col-8 flex align-items-center justify-content-center">
              <div class="col-12 status text-center">
                <div>
                  <em class="material-icons info-icon green-text">check_circle</em>
                  <h1 class="status-text">Success</h1>
                  <p class="py-3">Once data are provided, you will be notified through email.</p>
                  <PrimeButton
                    type="button"
                    @click="onClickToDataRequests()"
                    label="TO DATA REQUESTS"
                    class="uppercase p-button-outlined"
                  />
                </div>
              </div>
            </div>

            <div class="col-12 md:col-6 xl:col-4 bg-white radius-1 p-4">
              <h1 class="p-0">Data Request Summary</h1>
              <div class="summary-section border-bottom py-5">
                <h6 class="summary-section-heading m-0">{{ summarySectionReportingPeriodsHeading }}</h6>
                <p class="summary-section-data m-0 mt-3">2023, 2021</p>
              </div>
              <div class="summary-section border-bottom py-5">
                <h6 class="summary-section-heading m-0">{{ summarySectionFrameworksHeading }}</h6>
                <p class="summary-section-data m-0 mt-3">SFDR, Pathway to Paris</p>
              </div>
              <div class="summary-section py-5">
                <h6 class="summary-section-heading m-0">{{ summarySectionIdentifiersHeading }}</h6>
                <p class="summary-section-data m-0 mt-3">
                  <template v-for="identifier in identifiers" :key="identifier">
                    <div class="identifier">{{ identifier }}</div>
                  </template>
                </p>
              </div>
            </div>

            <!-- <div class="col-12" v-if="postBulkDataRequestObjectProcessed">
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
                      However, some identifiers couldn't be recognised.
                    </p>
                  </template>
                </MessageComponent>
                <MessageComponent v-else data-test="nonIdentifiersPassed" severity="light-error">
                  <template #left-icon>
                    <em class="material-icons info-icon p-message-icon red-text">error</em>
                  </template>
                  <template #text-info>
                    <h4>Data request couldn't be submitted.</h4>
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

            <div class="col-12" v-if="!submittingSucceded">
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

            <div class="col-12 md:col-8 xl:col-6" v-else>
              <div class="grid">
                <div class="col-12">
                  <BasicFormSection header="Select at least one reporting period">
                    <div class="flex flex-wrap mt-4 py-2">
                      <ToggleChipFormInputs :name="'listOfReportingPeriods'" :options="reportingPeriods" />
                    </div>
                  </BasicFormSection>

                  <BasicFormSection header="Select at least one framework">
                    <MultiSelectFormFieldBindData
                      data-test="selectFrameworkSelect"
                      placeholder="Select framework"
                      :options="availableFrameworks"
                      optionValue="value"
                      optionLabel="label"
                      v-model:selectedItemsBindInternal="selectedFrameworks"
                      innerClass="long"
                    />
                    <FormKit
                      :modelValue="selectedFrameworks"
                      type="text"
                      name="listOfFrameworkNames"
                      validation="required"
                      validation-label="List of framework names"
                      :validation-messages="{
                        required: 'Select at least one framework',
                      }"
                      :outer-class="{ 'hidden-input': true }"
                    />
                    <div data-test="addedFrameworks" class="radius-1 w-full">
                      <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                        >No Frameworks added yet</span
                      >
                      <span class="form-list-item" :key="it" v-for="it in selectedFrameworks">
                        {{ it }}
                        <em @click="removeItem(it)" class="material-icons">close</em>
                      </span>
                    </div>
                  </BasicFormSection>

                  <BasicFormSection header="Provide Company Identifiers">
                    <FormKit
                      v-model="identifiersInString"
                      type="textarea"
                      name="listOfCompanyIdentifiers"
                      validation="required"
                      validation-label="List of company identifiers"
                      :validation-messages="{
                        required: 'Provide at least one identifier',
                      }"
                      placeholder="E.g.: DE-000402625-0, SWE402626, DE-000402627-2, SWE402626,DE-0004026244"
                    />
                    <span class="gray-text font-italic">
                      Accepted identifiers: DUNS Number, LEI, ISIN & permID. Expected in comma separted format.
                    </span>
                  </BasicFormSection>
                </div>
                <div class="col-12 flex align-items-end">
                  <PrimeButton
                    type="submit"
                    label="Submit"
                    class="p-button p-button-sm d-letters ml-auto"
                    name="submit_request_button"
                  >
                    NEXT
                  </PrimeButton>
                </div>
              </div>
            </div> -->
          </div>
        </FormKit>
      </div>
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
import { type BulkDataRequestResponse, type BulkDataRequest } from "@clients/communitymanager";
import FailMessage from "@/components/messages/FailMessage.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import { AxiosError, type AxiosResponse, type AxiosPromise } from "axios";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import ToggleChip from "@/components/general/ToggleChip.vue";
import BasicFormSection from "@/components/general/BasicFormSection.vue";
import BulkDataResponseDialog from "@/components/general/BulkDataResponseDialog.vue";
import ToggleChipFormInputs from "@/components/general/ToggleChipFormInputs.vue";

export default defineComponent({
  name: "RequestBulkData",
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
    MarginWrapper,
    ToggleChip,
    BasicFormSection,
    BulkDataResponseDialog,
    ToggleChipFormInputs,
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
      bulkDataRequestModel: {},
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
      reportingPeriods: [
        { name: "2023", value: false },
        { name: "2022", value: false },
        { name: "2021", value: false },
        { name: "2020", value: false },
      ],
    };
  },

  computed: {
    humanizedSelectedFrameworks(): string[] {
      return this.selectedFrameworks.map((it) => humanizeStringOrNumber(it));
    },
    summarySectionReportingPeriodsHeading(): string {
      const len = this.reportingPeriods.filter((reportingPeriod) => reportingPeriod.value).length;
      return `${len} REPORTING PERDIOD${len > 1 ? "S" : ""}`;
    },
    summarySectionFrameworksHeading(): string {
      const len = this.selectedFrameworks.length;
      return `${len} FRAMEWORK${len > 1 ? "S" : ""}`;
    },
    summarySectionIdentifiersHeading(): string {
      const len = this.identifiers.length;
      return `${len} INDENTIFIER${len > 1 ? "S" : ""}`;
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
        listOfReportingPeriods: this.reportingPeriods
          .filter((reportingPeriod) => reportingPeriod.value)
          .map((reportingPeriod) => reportingPeriod.name),
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
      this.identifiersInString = this.identifiers.join(", ");
    },

    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      this.processInput();
      // this.messageCounter++;

      try {
        this.submittingInProgress = true;
        const bulkDataRequestObject = this.collectDataToSend();
        const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        // const response = await requestDataControllerApi.postBulkDataRequest(bulkDataRequestObject);
        const response = await mockResponse(bulkDataRequestObject);

        // if (response.data.rejectedCompanyIdentifiers.length) {
        //   this.openRequestModal(response.data);
        // }

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

    /**
     * Opens a pop-up to warn the user that the session will expire soon and offers a button to refresh it.
     * If the refresh button is clicked soon enough, the session is refreshed.
     * Else the text changes and tells the user that the session was closed.
     * @param response
     * @param responseData
     */
    openRequestModal(responseData: BulkDataRequestResponse): void {
      const rejectedIdentifiersCount = responseData.rejectedCompanyIdentifiers.length;

      this.$dialog.open(BulkDataResponseDialog, {
        props: {
          modal: true,
          closable: true,
          closeOnEscape: true,
          showHeader: true,
          header: `${rejectedIdentifiersCount} identifier${rejectedIdentifiersCount > 1 ? "s" : ""} can't be found.`,
          style: {
            width: "50vw",
          },
          breakpoints: {
            "1199px": "75vw",
            "575px": "90vw",
          },
        },
        data: {
          onItemChangeHandler: (eventName: "removed" | "undo", identifier: string) =>
            this.handleIdentifierChange(eventName, identifier),
          responseData,
        },
      });
    },

    /**
     * @param eventName whether "removed" or "undo"
     * @param identifier the identifier to be processed
     */
    handleIdentifierChange(eventName: string, identifier: string) {
      if (eventName === "removed") {
        this.identifiersInString = this.identifiers
          .filter((currentIdentifier) => currentIdentifier !== identifier)
          .join(", ");
      } else if (eventName === "undo") {
        this.identifiersInString += ", " + identifier;
      }

      this.processInput();
    },

    onClickToDataRequests() {
      console.log("BACK TO DATA REQUESTS");
    },
  },
  mounted() {
    this.retrieveAvailableFrameworks();
  },
});

// TODO: MOCKS - DELETE EVERYTHING BELOW

/**
 * @param bulkDataRequestObject
 * @returns mock promise response
 */
async function mockResponse(bulkDataRequestObject: any): AxiosPromise<BulkDataRequestResponse> {
  const identifiers = splitArrayRandomly(bulkDataRequestObject.listOfCompanyIdentifiers);

  const data = {
    message: "Mock message!",
    rejectedCompanyIdentifiers: identifiers[0],
    acceptedCompanyIdentifiers: identifiers[1],
  };
  return Promise.resolve({ data } as AxiosResponse);
}

function splitArrayRandomly(inputArray) {
  return inputArray.reduce(
    ([arr1, arr2], item) => {
      return Math.random() > 0.5 ? [[...arr1, item], arr2] : [arr1, [...arr2, item]];
    },
    [[], []],
  );
}
</script>

<style scoped lang="scss">
.uploadFormWrapper {
  min-height: calc(100vh - 200px);

  .status {
    .status-text {
      font-weight: 700;
      font-size: 48px;
      line-height: 24px;
      letter-spacing: 0.25px;
    }
    .info-icon {
      font-size: 48px;
    }
  }

  div.summary-section {
    &.border-bottom {
      border-bottom: 1px solid #dadada;
    }
    .summary-section-heading {
      font-weight: 400;
      font-size: 14px;
      line-height: 20px;
    }
    .summary-section-data {
      font-weight: 700;
    }
  }
}

.no-framework {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
