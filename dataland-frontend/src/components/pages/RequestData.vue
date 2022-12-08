<template>
  <TheHeader />
  <TheContent class="pl-0 pt-0 min-h-screen surface-800 relative">
    <h2 class="mt-0 pt-4 pl-4 text-left">Request companies data for EU Taxonomy, SFDR or LkSG.</h2>

    <div class="pl-4 col-6 text-left">
      <h3>Download</h3>
      <InfoCard class="mb-3 font-medium">
        Download and fill the EXCEL template with your request and upload it below.
        <div class="mt-3">
          <a
            class="text-primary"
            :href="'excel-files/' + fileNameOfExcelTemplate"
            id="download-data-request-excel-template"
          >
            DOWNLOAD - EXCEL TEMPLATE .XLS
          </a>
        </div>
      </InfoCard>
    </div>

    <div class="pl-4 col-6 text-left">
      <h3>Upload</h3>

      <FileUpload
        name="data-request-upload[]"
        mode="advanced"
        url="./upload"
        :custom-upload="true"
        :max-file-size="5000000"
        :file-limit="10"
        accept=".xlsx"
        :multiple="true"
        @uploader="uploadDataRequest"
      >
        <template #header="{ chooseCallback, uploadCallback, clearCallback, files }">
          <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
            <div class="flex gap-2">
              <Button @click="chooseCallback()" icon="pi pi-images" class="p-button-rounded"></Button>
              <Button
                @click="uploadCallback()"
                icon="pi pi-cloud-upload"
                class="p-button-rounded p-button-success"
                :disabled="!files || files.length === 0"
              ></Button>
              <Button
                @click="clearCallback()"
                icon="pi pi-times"
                class="p-button-rounded p-button-danger"
                :disabled="!files || files.length === 0"
              ></Button>
            </div>
            <ProgressBar
              :value="totalSizePercent"
              :showValue="false"
              :class="['md:w-20rem h-1rem w-full md:ml-auto', { 'exceeded-progress-bar': totalSizePercent > 100 }]"
              ><span class="white-space-nowrap">{{ totalSize }}B / 1Mb</span></ProgressBar
            >
          </div>
        </template>
        <template #content="{ files, uploadedFiles, onUploadedFileRemove, onFileRemove }">
          <div v-if="files.length > 0">
            <h5>Pending</h5>
            <div class="flex flex-wrap p-5 gap-5">
              <div
                v-for="(file, index) of files"
                :key="file.name + file.type + file.size"
                class="card m-0 px-6 flex flex-column border-1 surface-border align-items-center gap-3"
              >
                <div>
                  <img role="presentation" :alt="file.name" :src="file.objectURL" height="50" class="shadow-2" />
                </div>
                <span class="font-semibold">{{ file.name }}</span>
                <div>{{ formatSize(file.size) }}</div>
                <Badge value="Pending" severity="warning" />
                <Button
                  icon="pi pi-times"
                  @click="onRemoveTemplatingFile(file, onFileRemove, index)"
                  class="p-button-outlined p-button-danger p-button-rounded"
                />
              </div>
            </div>
          </div>

          <div v-if="uploadedFiles.length > 0">
            <h5>Completed</h5>
            <div class="flex flex-wrap p-0 sm:p-5 gap-5">
              <div
                v-for="(file, index) of uploadedFiles"
                :key="file.name + file.type + file.size"
                class="card m-0 px-6 flex flex-column border-1 surface-border align-items-center gap-3"
              >
                <div>
                  <img role="presentation" :alt="file.name" :src="file.objectURL" width="100" class="shadow-2" />
                </div>
                <span class="font-semibold">{{ file.name }}</span>
                <div>{{ formatSize(file.size) }}</div>
                <Badge value="Completed" class="mt-3" severity="success" />
                <Button
                  icon="pi pi-times"
                  @click="onUploadedFileRemove(index)"
                  class="p-button-outlined p-button-danger p-button-rounded"
                />
              </div>
            </div>
          </div>
        </template>
        <template #empty>
          <div class="flex align-items-center justify-content-center flex-column">
            <i class="pi pi-cloud-upload border-2 border-circle p-5 text-8xl text-400 border-400" />
            <p class="mt-4 mb-0">+ Drag and drop your file or BROWSE</p>
          </div>
        </template>
      </FileUpload>
    </div>

    <div class="pl-4 col-6 text-left">
      <h3>Your Files</h3>
      No file uploaded
    </div>

    <div class="pl-4 col-6 text-left">
      <h3>Additional Settings</h3>
      <Checkbox v-model="hideName" :binary="true" aria-label="Salami" />
    </div>
  </TheContent>
</template>

<script lang="ts">
import InfoCard from "@/components/general/InfoCard.vue";
import FileUpload from "primevue/fileupload";
import Checkbox from "primevue/checkbox";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";

export default defineComponent({
  name: "RequestData",
  components: { TheHeader, TheContent, InfoCard, FileUpload, Checkbox },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      fileNameOfExcelTemplate: "Dataland_Request_Template.xlsx",
      uploadInProgress: false,
      uploadFinished: false,
      hideName: false,
    };
  },
  mounted() {
    console.log("mounted"); // TODO
  },
  watch: {
    hideName() {
      console.log("hideName changed");
    },
  },
  methods: {
    uploadDataRequest() {
      console.log("Upload should take place");
    },
  },
});
</script>
