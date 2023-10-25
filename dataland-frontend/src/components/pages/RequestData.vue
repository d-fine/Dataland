<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="min-h-screen paper-section">
      <FormKit :actions="false" type="form" id="" name="">
        <div class="pl-4 col-5 text-left">
          <h2 class="py-4">Request framework data for one or multiple companies</h2>

          <div id="disclaimer-section" class="mb-6">
            <h4 class="red-text fw-normal">
              Disclaimer: A unique mapping between company and identifier is only implemented for LEI yet!
            </h4>
          </div>

          <div id="framework-section" class="mb-6">
            <h4>Please select the frameworks for which you want to request data:</h4>
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

          <div id="company-section" class="mb-6">
            <h4>Please enter at least one identifier for each desired company:</h4>
            <div class="form-field">
              <FormKit type="list" v-model="existingElements">
                <FormKit type="group" v-for="id in listOfElementIds" :key="id">
                  <div data-test="identifiers-section" class="formListSection">
                    <em data-test="removeButton" @click="removeItem(id)" class="material-icons close-section">close</em>
                    <FormKit type="group" name="Identifier">
                      <FormKit class="long" type="text" name="lei" placeholder="LEI" />
                      <div class="next-to-each-other">
                        <FormKit type="text" name="isin" placeholder="ISIN" />
                        <FormKit type="text" name="permId" placeholder="permID" />
                      </div>
                    </FormKit>
                  </div>
                </FormKit>
                <PrimeButton
                  data-test="addButton"
                  label="ADD NEW Company"
                  class="p-button-text"
                  icon="pi pi-plus"
                  @click="addItem"
                />
              </FormKit>
            </div>
          </div>
        </div>
      </FormKit>
      <div class="m-0 fixed bottom-0 surface-900 h-4rem w-full align-items-center">
        <div class="flex justify-content-end flex-wrap">
          <div class="flex align-items-center justify-content-center m-2">
            <PrimeButton
              label="Submit"
              class="uppercase p-button p-button-sm d-letters justify-content-center w-6rem mr-3"
              name="submit_request_button"
              :disabled="selectedFrameworks.length == 0 || existingElements.length == 0"
            >
              Submit
            </PrimeButton>
          </div>
        </div>
      </div>
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
  },
  /*
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },*/ // TODO commented out for now, but required later when a POST request is sent
  props: {
    selectedFrameworks: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
  },
  data() {
    return {
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      existingElements: [] as unknown[],
      listOfElementIds: [0],
      idCounter: 0,

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
