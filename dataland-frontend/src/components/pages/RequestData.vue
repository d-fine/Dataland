<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="min-h-screen paper-section">
      <FormKit :actions="false" type="form" id="" name="">
        <div class="pl-4 col-5 text-left">
          <h2 class="py-4">Request Data</h2>

          <div id="disclaimer-section" class="mb-6">
            <h4>
              Insert the company identifiers into the text field below. Recognised identifiers are: LEI, ISIN and
              PermID.
            </h4>
          </div>

          <div id="framework-section" class="mb-6">
            <h4>Please select the framework(s) for which you want to request data:</h4>
            <FrameworkDataSearchDropdownFilter
              v-model="selectedFrameworksInt"
              ref="frameworkFilter"
              :available-items="availableFrameworks"
              filter-name="Framework"
              filter-id="framework-filter"
              filter-placeholder="Search frameworks"
              class="ml-3"
            />
          </div>

          <div id="framework-section-two" class="mb-6">
            <h4>Please select the framework(s) for which you want to request data two:</h4>
            <MultiSelect
              v-model="selectedFrameworksInt"
              placeholder="Select Frameworks"
              :options="availableFrameworks"
              class="ml-3"
            />
          </div>

          <div id="company-section" class="mb-6">
            <div class="form-field">
              <FormKit
                v-model="input"
                type="textarea"
                name="input"
                placeholder="Insert identifiers here. Separated by either comma, space, semicolon or linebreak."
              />
            </div>
          </div>
        </div>
      </FormKit>
      <PrimeButton
        @click="submitRequest"
        label="Submit"
        class="uppercase p-button p-button-sm d-letters justify-content-center w-6rem mr-3"
        name="submit_request_button"
      >
        Submit Data Request
      </PrimeButton>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { type DataTypeEnum } from "@clients/backend";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import FrameworkDataSearchDropdownFilter from "@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import TheFooter from "@/components/generics/TheFooter.vue";
import { type FrameworkSelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import MultiSelect from "primevue/multiselect";

export default defineComponent({
  name: "RequestData",
  components: {
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    TheFooter,
    FrameworkDataSearchDropdownFilter,
    PrimeButton,
    FormKit,
    MultiSelect,
  },
  /*
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },*/ // TODO commented out for now, but required later when a POST request is sent
  /*props: {
    selectedFrameworks: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
  },*/
  data() {
    return {
      displayNames: [] as Array<string>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      existingElements: [] as unknown[],
      listOfElementIds: [0],
      idCounter: 0,
      identifiers: [] as Array<string>,
      input: "" as string,
      selectedFrameworks: [] as Array<DataTypeEnum>,

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
     * Submits the data request to the request service
     */
    submitRequest() {
      this.processInput();
    },

    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveAvailableFrameworks() {
      this.availableFrameworks = ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
        return {
          frameworkDataType: dataTypeEnum,
          displayName: humanizeStringOrNumber(dataTypeEnum),
          disabled: false,
        };
      });
    },

    /**
     * Remove Object from array
     * @param id - the id of the object in the array
     */
    removeItem(id: number) {
      this.listOfElementIds = this.listOfElementIds.filter((el) => el !== id);
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
    for (let i = 1; i < this.existingElements.length; i++) {
      this.addItem();
    }
  },
});
</script>

<style scoped>
a,
img:hover {
  cursor: pointer;
}
</style>
