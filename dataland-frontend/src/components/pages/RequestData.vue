<template>
  <h2 class="pl-4 text-left">Request companies data for EU Taxonomy, SFDR or LkSG.</h2>

  <div class="pl-4 col-6 text-left">
    <h3>Download</h3>
    <InfoCard class="mb-3 font-medium">
      Download and fill the EXCEL template with your request and upload it below.
      <div class="mt-3">
        <a :href="'excel-files/' + fileNameOfExcelTemplate" id="download-data-request-excel-template">
          DOWNLOAD - EXCEL TEMPLATE .XLS
        </a>
      </div>
    </InfoCard>
  </div>

  <div class="pl-4 col-6 text-left">
    <h3>Upload</h3>

    <FileUpload
      name="blub"
      url="./upload"
      :custom-upload="true"
      :max-file-size="5000000"
      :file-limit="10"
      accept=".xlsx"
      :multiple="true"
      @uploader="uploadDataRequest"
    >
      + Drag and drop your file or BROWSE
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
</template>

<script lang="ts">
import InfoCard from "@/components/general/InfoCard.vue";
import FileUpload from "primevue/fileupload";
import Checkbox from "primevue/checkbox";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "RequestData",
  components: { InfoCard, FileUpload, Checkbox },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      fileNameOfExcelTemplate: "Dataland_Request_Template.xlsx",
      waitingForData: true,
      hideName: false,
    };
  },
  mounted() {},
  watch: {
    somethingToWatch() {},
  },
  methods: {
    uploadDataRequest() {
      console.log("Upload should take place");
    },
  },
});
</script>
