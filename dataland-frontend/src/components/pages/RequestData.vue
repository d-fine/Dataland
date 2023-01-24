<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 min-h-screen paper-section">
      <ProgressBar
        v-if="submissionInProgress || submissionFinished"
        :processHasStarted="submissionInProgress"
        :processIsFinished="submissionFinished"
        :wasProcessSuccessful="isInviteSuccessful"
        :processResultMessage="inviteResultMessage"
        :progressTitle="submissionProgressTitle"
        :progressInPercent="formatProgressPercentage(uploadProgressInPercent)"
      >
        <template #options>
          <div v-if="submissionFinished" class="mt-6 pl-3 pr-3" id="new-data-request">
            <InfoCard>
              <div class="flex justify-content-between align-items-center">
                <span class="font-medium text-left col-6"
                  >Submit a new data request for more companies or frameworks</span
                >
                <div class="flex align-items-center" @click="createNewRequest">
                  <a class="pr-3 text-primary font-semibold">NEW DATA REQUEST</a>
                  <img src="@/assets/images/elements/add_button.svg" alt="remove-file-button" />
                </div>
              </div>
            </InfoCard>
          </div>
        </template>
      </ProgressBar>

      <div v-else>
        <div class="pl-4 col-5 text-left">
          <h3 class="py-4">Request companies data for EU Taxonomy, SFDR or LkSG.</h3>

          <div id="download-section" class="mb-6">
            <h2>Download</h2>
            <InfoCard class="mb-3 font-medium">
              Download and fill the EXCEL template with your request and upload it below.
              <div class="mt-3">
                <a
                  class="text-primary"
                  href="/Dataland_Request_Template.xlsx"
                  id="download-data-request-excel-template"
                  download
                >
                  DOWNLOAD - EXCEL TEMPLATE .XLS
                </a>
              </div>
            </InfoCard>
          </div>

          <div id="upload-section" class="mb-6">
            <h2>Upload</h2>

            <FileUpload
              ref="fileUpload"
              mode="advanced"
              :auto="false"
              accept=".xlsx"
              :maxFileSize="maxFileSize"
              :fileLimit="1"
              @select="handleSelectFile"
              @remove="clearSelection"
            >
              <template #header>
                <div></div>
              </template>

              <template #content="{ files, removeFileCallback, messages }">
                <FileSelectMessage v-for="msg of messages" :key="msg" severity="error" @close="onMessageClose"
                  >{{ msg }}
                </FileSelectMessage>
                <div v-if="files.length > 0">
                  <p class="m-0">Your selected Excel file for the upload:</p>
                  <div class="flex align-items-center justify-content-between">
                    <div>
                      <span class="font-semibold mr-2">{{ files[0].name }}</span>
                      <span class="font-light mr-4">{{ "(" + formatBytes(files[0].size) + ")" }}</span>
                    </div>
                    <img
                      src="@/assets/images/elements/remove_button.svg"
                      alt="remove-file-button"
                      @click="removeFileCallback()"
                    />
                  </div>
                </div>
              </template>

              <template #empty>
                <div class="flex align-items-center justify-content-center flex-column surface-0">
                  <em class="pi pi-cloud-upload p-3 text-6xl text-400" />
                  <div class="flex align-items-center">
                    <p>+ Drag and drop a file or</p>
                    <a class="text-primary font-medium pl-1" @click="chooseFiles">BROWSE</a>
                  </div>
                </div>
              </template>
            </FileUpload>
          </div>

          <div id="settings-section">
            <h2>Additional Settings</h2>
            <div>
              <Checkbox class="mr-2" id="chkbox1" v-model="hideName" :binary="true" />
              <label class="font-medium" for="chkbox1">Hide my name from the data request.</label>
            </div>
          </div>
        </div>
        <div class="m-0 fixed bottom-0 surface-900 h-4rem w-full align-items-center">
          <div class="flex justify-content-end flex-wrap">
            <div class="flex align-items-center justify-content-center m-2">
              <PrimeButton
                label="Submit"
                class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary w-6rem mr-3"
                name="submit_request_button"
                @click="handleSubmission"
                :disabled="!selectedFile"
              >
                Submit
              </PrimeButton>
            </div>
          </div>
        </div>
      </div>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import FileUpload, { FileUploadSelectEvent } from "primevue/fileupload";
import Message from "primevue/message";
import Checkbox from "primevue/checkbox";
import PrimeButton from "primevue/button";
import { defineComponent, inject, ref } from "vue";
import { AxiosResponse } from "axios";
import Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { InviteMetaInfoEntity } from "@clients/backend";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import InfoCard from "@/components/general/InfoCard.vue";
import ProgressBar from "@/components/general/ProgressBar.vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { formatBytesUserFriendly, roundNumber } from "@/utils/NumberConversionUtils";
import { UPLOAD_FILE_SIZE_DISPLAY_DECIMALS, UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default defineComponent({
  name: "RequestData",
  components: {
    AuthenticationWrapper,
    PrimeButton,
    TheHeader,
    TheContent,
    InfoCard,
    ProgressBar,
    FileUpload,
    FileSelectMessage: Message,
    Checkbox,
    DatalandFooter,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      fileUpload: ref<typeof FileUpload>(),
    };
  },
  data() {
    return {
      uploadProgressInPercent: 0,
      isInviteSuccessful: false,
      inviteResultMessage: "No invite result message available.",
      submissionFinished: false,
      submissionInProgress: false,
      maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      selectedFile: null as null | File,
      hideName: false,
    };
  },

  computed: {
    submissionProgressTitle() {
      if (this.submissionFinished) {
        if (this.isInviteSuccessful) {
          return "Success";
        } else {
          return "Submission failed";
        }
      } else {
        return "Submitting file";
      }
    },
  },

  methods: {
    /**
     * Refreshes the page to allow the user to make a new data request
     */
    createNewRequest() {
      this.$router.go();
    },

    /**
     * Clears the selected file
     */
    clearSelection() {
      this.selectedFile = null;
    },

    /**
     * Called when a new file is selected in the file selector. Updates the selected file.
     * Overwrites any currently selected file if present.
     *
     * @param event the file upload event
     */
    handleSelectFile(event: FileUploadSelectEvent) {
      const arrayOfSelectedFiles = event.files as Array<File>;
      if (arrayOfSelectedFiles.length > 1) {
        this.selectedFile = arrayOfSelectedFiles[1];
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access, @typescript-eslint/no-unsafe-call
        this.fileUpload?.files.shift();
      } else {
        this.selectedFile = arrayOfSelectedFiles[0];
      }
    },

    /**
     * Formats the size of a file in a human-readable format for the UI
     *
     * @param bytes the size of the selected file
     * @returns a humanized version of the size specified by bytes
     */
    formatBytes(bytes: number): string {
      return formatBytesUserFriendly(bytes, UPLOAD_FILE_SIZE_DISPLAY_DECIMALS);
    },

    /**
     * Computes the upload progress in whole percents for the progress bar
     *
     * @param percentage the input percentage
     * @returns the percentage rounded to whole numbers
     */
    formatProgressPercentage(percentage: number) {
      return roundNumber(percentage, 0);
    },

    /**
     * Opens the OSes file browser
     */
    chooseFiles() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.fileUpload?.choose();
    },

    /**
     * Retrieves the currently selected file
     *
     * @returns the currently selected file
     */
    getSelectedFile(): File {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access,@typescript-eslint/no-unsafe-return
      return this.fileUpload?.files[0];
    },

    /**
     * Called when the user hits submit. Enables the progress bar and uploads the file.
     */
    async handleSubmission() {
      this.submissionInProgress = true;
      await this.uploadSelectedFile();
      this.submissionFinished = true;
      this.submissionInProgress = false;
    },

    /**
     * Updates the UI to reflect the result of the file upload
     *
     * @param response the result of the file upload request
     */
    readInviteStatusFromResponse(response: AxiosResponse<InviteMetaInfoEntity>) {
      this.isInviteSuccessful = response.data.wasInviteSuccessful ?? false;
      this.inviteResultMessage = response.data.inviteResultMessage ?? "No response from server.";
    },

    /**
     * Uploads the selected file. Updates the UI after the upload has completed
     */
    async uploadSelectedFile(): Promise<void> {
      const selectedFile = this.getSelectedFile();
      try {
        const inviteControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getInviteControllerApi();
        const response = await inviteControllerApi.submitInvite(this.hideName, selectedFile, {
          onUploadProgress: (progressEvent) => {
            this.uploadProgressInPercent = (progressEvent.loaded / progressEvent.total) * 100;
          },
        });
        this.readInviteStatusFromResponse(response);
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>

<style scoped>
a,
img:hover {
  cursor: pointer;
}
</style>
