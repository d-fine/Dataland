<template>
  <TheHeader />
  <TheContent class="pl-0 min-h-screen surface-800">
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
              download>
              DOWNLOAD - EXCEL TEMPLATE .XLS
            </a>
          </div>
        </InfoCard>
      </div>

      <div id="upload-section" class="mb-6">
        <h2>Upload</h2>

        <FileUpload
          mode="advanced"
          name="data-request-upload[]"
          :auto=false
          :custom-upload=true
          @uploader="uploadFile"
          accept=".xlsx"
          :multiple=true
          :max-file-size=5000000
          :fileLimit=10
        >
          <template #empty>
            <div class="flex align-items-center justify-content-center flex-column">
              <i class="pi pi-cloud-upload border-2 border-circle p-5 text-8xl text-400 border-400" />
              <p class="mt-4 mb-0">+ Drag and drop your file or BROWSE</p>
            </div>
          </template>
        </FileUpload>
      </div>

      <div id="files-section" class="mb-6">
        <h2>Your Files</h2>
        <p class="text-gray-800">No file uploaded</p>
      </div>

      <div id="settings-section">
        <h2>Additional Settings</h2>
        <div>
          <Checkbox class="mr-2" id="chkbox1" v-model="hideName" :binary="true"/>
          <label class="font-medium" for="chkbox1">Hide my name from the data request.</label>
        </div>
      </div>
    </div>
  </TheContent>
</template>

<script lang="ts">
import InfoCard from "@/components/general/InfoCard.vue";
import FileUpload from "primevue/fileupload";
import Checkbox from "primevue/checkbox";
import ProgressBar from "primevue/checkbox";
import Badge from "primevue/checkbox";
import Button from "primevue/checkbox";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";

export default defineComponent({
  name: "RequestData",
  components: { TheHeader, TheContent, InfoCard, FileUpload, Checkbox, ProgressBar, Badge, Button },
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
    uploadFile(event) {
      const file = event.files
      console.log("Upload should take place");
    },
    chooseCallback() {
      console.log("Choosecallback")
    },
    uploadCallback() {
      console.log("Uploadcallback")
    },
    clearCallback() {
      console.log("Clearcallback")
    }
  },
});

// TODO add AuthenticationWrapper to component when done designing
</script>
