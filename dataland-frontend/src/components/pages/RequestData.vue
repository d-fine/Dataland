<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 min-h-screen surface-800">
      <div class="pl-4 col-5 text-left">
        <h3 class="py-4">Request companies data for EU Taxonomy, SFDR or LkSG.</h3>

        <div id="download-section" class="mb-6">
          <h2>Download</h2>
          <InfoCard class="mb-3 font-medium">
            Download and fill the EXCEL template with your request and upload it below.
            <div class="mt-3">
              <a class="text-primary"
                :href="'/' + fileNameOfExcelTemplate"
                id="download-data-request-excel-template"
                download>
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
            :multiple="true"
            :max-file-size=maxFileSize
            :fileLimit=fileLimit
          >
            <template #header="{ files, clearCallback }">
              <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
                <div class="flex gap-2">
                  <PrimeButton
                    @click="clearCallback()"
                    label="Clear all"
                    class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary mr-9"
                    :disabled="!files || files.length === 0"
                  />
                </div>
              </div>
            </template>

            <template #content="{ files, removeFileCallback }">
              <div v-if="files.length > 0">
                <p class="m-0">Selected files for upload:</p>
                <div
                  v-for="file of files"
                  :key="file.name + file.type + file.size"
                  class="flex align-items-center justify-content-between"
                >
                  <div>
                    <span class="font-semibold mr-2">{{ file.name }}</span>
                    <span class="font-light mr-4">{{ "(" + formatBytes(file.size) + ")" }}</span>
                  </div>
                  <PrimeButton
                    label="Clear"
                    @click="removeFileCallback()"
                    class="mt-2 p-button-outlined p-button-danger p-button-rounded"
                  />
                </div>

                <div class="mt-4 flex align-items-center justify-content-center flex-column">
                  <i class="pi pi-cloud-upload p-3 text-6xl text-400" />
                  <div class="flex align-items-center">
                    <p>+ Add more files by drag and drop or</p>
                    <a class="text-primary font-medium pl-1" @click="chooseFiles">BROWSE</a>
                  </div>
                </div>
              </div>
            </template>

            <template #empty>
              <div class="flex align-items-center justify-content-center flex-column">
                <i class="pi pi-cloud-upload p-3 text-6xl text-400" />
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
    </TheContent>
    <PrimeButton label="TESTUPLOAD" @click="uploadAllSelectedFiles" />
    <TheBottom />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import InfoCard from "@/components/general/InfoCard.vue";
import FileUpload from "primevue/fileupload";
import Checkbox from "primevue/checkbox";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheBottom from "@/components/generics/TheBottom.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import PrimeButton from "primevue/button";
import { humanizeBytes } from "@/utils/StringHumanizer";
import {
  UPLOAD_FILE_SIZE_DECIMALS,
  EXCEL_TEMPLATE_FILE_NAME,
  UPLOAD_MAX_FILE_SIZE,
  UPLOAD_FILES_LIMIT
} from "@/utils/Constants";

export default defineComponent({
  name: "RequestData",
  components: {
    TheBottom,
    AuthenticationWrapper,
    PrimeButton,
    TheHeader,
    TheContent,
    InfoCard,
    FileUpload,
    Checkbox,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      searchBarAndFiltersContainer: ref<typeof FileUpload>(),
    };
  },
  data() {
    return {
      fileNameOfExcelTemplate: EXCEL_TEMPLATE_FILE_NAME,
      maxFileSize: UPLOAD_MAX_FILE_SIZE,
      fileLimit: UPLOAD_FILES_LIMIT,
      hideName: false,
    };
  },
  methods: {
    formatBytes(bytes: number): string {
      return humanizeBytes(bytes, UPLOAD_FILE_SIZE_DECIMALS);
    },

    chooseFiles() {
      this.$refs.fileUpload.choose();
    },

    getAllSelectedFiles() {
      return this.$refs.fileUpload.files;
    },

    async uploadAllSelectedFiles(): Promise<void> {
      const allSelectedFiles = this.getAllSelectedFiles();
      try {
        const fileControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getFileControllerApi();
        await fileControllerApi.submitInvitation(this.hideName, allSelectedFiles);
      } catch (error) {
        console.error(error);
      } finally {
        this.$refs.fileUpload.clear(); // can be omitted if router push active
        // router push to new page with progressbar
      }
    },
  },
});

// TODO delete the TESTUPLOAD button at the end of dev
</script>

<style scoped>
a:hover {
  cursor: pointer;
}
</style>
