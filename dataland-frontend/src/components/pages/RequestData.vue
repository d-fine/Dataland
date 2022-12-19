<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 min-h-screen surface-800">
      <div v-if="submissionFinished">
        <p>Submission finished!</p>
        <p>{{ "Invite successful?: " + isInviteSuccessful}}</p>
        <p>{{ "Invite result message: " + inviteResultMessage }}</p>
      </div>
      <div v-else-if="submissionInProgress">
        Submission is in progress. Please wait
      </div>
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
                :href="'/' + fileNameOfExcelTemplate"
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
            :fileLimit=1
            @select="handleSelectFile"
            @clear="clearSelection"
            @remove="clearSelection"
          >
            <template #header>
              <div></div>
            </template>

            <template #content="{ files, removeFileCallback, messages }">
              <FileSelectMessage v-for="msg of messages" :key="msg" severity="error" @close="onMessageClose">{{ msg }}
              </FileSelectMessage>
              <div v-if="files.length > 0">
                <p class="m-0">Your selected Excel file for the upload:</p>
                <div
                  class="flex align-items-center justify-content-between"
                >
                  <div>
                    <span class="font-semibold mr-2">{{ files[0].name }}</span>
                    <span class="font-light mr-4">{{ "(" + formatBytes(files[0].size) + ")" }}</span>
                  </div>
                  <img src="@/assets/images/elements/remove_button.svg" alt="remove-file-button" @click="removeFileCallback()"/>
                </div>
              </div>
            </template>

            <template #empty>
              <div class="flex align-items-center justify-content-center flex-column">
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
      <div class="m-0 fixed bottom-0 surface-900 h-4rem w-full align-items-center ">
        <div class="flex justify-content-end flex-wrap">
      <div class="flex align-items-center justify-content-center m-2">
        <PrimeButton label="Reset"
                     class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center surface-900 w-6rem mr-3"
                     name="reset_request_button"
                     @click="openModal">
          Reset
        </PrimeButton>
        </div>
        <div class="flex align-items-center justify-content-center m-2">
        <PrimeButton label="Submit"
                     class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary w-6rem mr-3"
                     name="submit_request_button"
                     @click="handleSubmission"
                     :disabled="!selectedFile">
          Submit
        </PrimeButton>
        </div>
          </div>
    </div>
      <Dialog header="Reset Request Data" v-model:visible="displayModal" :style="{width: '34vw'}" :modal="true" :showHeader="false" closeIcon="pi pi-times-circle">
        <div class="grid">
        <Button class="bg-white align-content-end col-1 col-offset-11 ml-9 mt-2 buttonstyle" > <span @click="closeModal"  class="p-dialog-header-close-icon pi pi-times-circle hovericon iconstyle"></span></Button>
        </div> <h2 class="mt-0 mb-5">Reset Request Data</h2>

        <p >Are you sure you want to reset your request?</p>
          <p class="font-bold">This will remove all the selected files.</p>
          <div class="flex justify-content-end flex-wrap mb-2">
            <div class="flex align-items-center justify-content-center m-2"><PrimeButton label="No"  class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white w-6rem" @click="closeModal">Cancel</PrimeButton></div>
            <div class="flex align-items-center justify-content-center m-2"><PrimeButton label="Yes"  class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary w-6rem" @click="resetPage">Confirm</PrimeButton></div>
          </div>
      </Dialog>
      </div>
    </TheContent>

  </AuthenticationWrapper>
</template>

<script lang="ts">
import InfoCard from "@/components/general/InfoCard.vue";
import FileUpload from "primevue/fileupload";
import Message from "primevue/message";
import Dialog from "primevue/dialog"
import Checkbox from "primevue/checkbox";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import PrimeButton from "primevue/button";
import { humanizeBytes } from "@/utils/StringHumanizer";
import {
  UPLOAD_FILE_SIZE_DISPLAY_DECIMALS,
  EXCEL_TEMPLATE_FILE_NAME,
  UPLOAD_MAX_FILE_SIZE_IN_BYTES,
} from "@/utils/Constants";
import {AxiosResponse} from "axios";
import {InviteMetaInfoEntity} from "@clients/backend";

export default defineComponent({
  name: "RequestData",
  components: {
    AuthenticationWrapper,
    PrimeButton,
    TheHeader,
    TheContent,
    InfoCard,
    FileUpload,
    FileSelectMessage: Message,
    Checkbox,
    Dialog,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      searchBarAndFiltersContainer: ref<typeof FileUpload>(),
    };
  },
  data() {
    return {
      isInviteSuccessful: false,
      inviteResultMessage: "",
      submissionFinished: false,
      submissionInProgress: false,
      fileNameOfExcelTemplate: EXCEL_TEMPLATE_FILE_NAME,
      maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      selectedFile: null,
      hideName: false,
      displayModal: false,
    };
  },
  methods: {
    clearSelection(){
      this.selectedFile = null
    },

    handleSelectFile(event) {
      if (event.files.length > 1) {
        this.selectedFile = event.files[1]
        this.$refs.fileUpload.files.shift()
      }
      else {
        this.selectedFile = event.files[0]
      }
    },

    openModal() {
      this.displayModal = true;
    },
    closeModal() {
      this.displayModal = false;
    },
    formatBytes(bytes: number): string {
      return humanizeBytes(bytes, UPLOAD_FILE_SIZE_DISPLAY_DECIMALS);
    },

    chooseFiles() {
      this.$refs.fileUpload.choose();
    },

    getSelectedFile(): File {
      return this.$refs.fileUpload.files[0];
    },

    handleSubmission(){
      this.submissionInProgress = true
      this.uploadAllSelectedFiles()
      this.submissionFinished = true
      this.submissionInProgress = false
    },

    readInviteStatusFromResponse(response: AxiosResponse<InviteMetaInfoEntity>) {
      this.isInviteSuccessful = response.data.wasInviteSuccessful ?? false
      this.inviteResultMessage = response.data.inviteResultMessage ?? "No response from server."
    },

    async uploadAllSelectedFiles(): Promise<void> {
      const selectedFile = this.getSelectedFile();
      try {
        const inviteControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getInviteControllerApi();
        const response = await inviteControllerApi.submitInvite(this.hideName, selectedFile);
        this.readInviteStatusFromResponse(response)
      } catch (error) {
        console.error(error);
      }
    },
    async resetPage(): Promise<void> {
      this.$refs.fileUpload.clear()
      this.displayModal = false;
      this.hideName = false;
    },
  }
})

</script>

<style scoped>
a, img:hover {
  cursor: pointer;
}
.buttonstyle {
  border:none;
  color:#1b1b1b;
}
.iconstyle{
  color:white;
  background-color: #958D7C;
  border-radius: 50%;
}
.hovericon {
  cursor: pointer;
}
</style>
