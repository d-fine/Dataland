<template>
  <div v-if="!editMode" class="col-3 p-3 topicLabel">
    <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
    <p>Please upload all relevant reports for this dataset in the PDF format.</p>
  </div>
  <!-- Select company reports -->
  <div class="col-9 formFields uploaded-files">
    <h3 class="mt-0">Select company reports</h3>
    <FileUpload
      name="fileUpload"
      ref="fileUpload"
      accept=".pdf"
      @select="onSelectedFiles"
      :multiple="true"
      :maxFileSize="maxFileSize"
      invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
      :auto="false"
    >
      <template #header="{ chooseCallback }">
        <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
          <div class="flex gap-2">
            <PrimeButton
              data-test="upload-files-button"
              @click="chooseCallback()"
              icon="pi pi-upload"
              :label="editMode ? 'ADD REPORTS' : 'SELECT REPORTS'"
            />
          </div>
        </div>
      </template>
      <template #content="{ files, removeFileCallback }">
        <div v-if="files.length > 0" data-test="uploaded-files">
          <div
            v-for="(file, index) of files"
            :key="file.name + index"
            class="flex w-full align-items-center file-upload-item"
          >
            <span data-test="uploaded-files-title" class="font-semibold flex-1">{{ file.name }}</span>
            <div data-test="uploaded-files-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(file.size, 3) }}
            </div>
            <PrimeButton
              data-test="uploaded-files-remove"
              icon="pi pi-times"
              @click="removeReportFromFilesToUpload(file, removeFileCallback, index)"
              class="p-button-rounded"
            />
          </div>
        </div>
      </template>
    </FileUpload>
  </div>
  <FormKit name="referencedReports" type="group">
    <div class="uploadFormSection">
      <!-- List of company reports to upload -->
      <div v-for="(file, index) of filesToUpload" :key="file.name" class="col-9 formFields">

        <div v-if="file.nameAlreadyExists">
          <p>File with name: <h3>{{file.name.split(".")[0]}}</h3> Already exist. Please upload file with different name.</p>
        </div>
        <div v-else>
          <div class="form-field-label">
            <h3 class="mt-0">{{ file.name.split(".")[0] }}</h3>
          </div>
          <FormKit :name="file.name.split('.')[0]" type="group">
            <!-- Date of the report -->
            <div class="form-field">
              <UploadFormHeader
                  :name="euTaxonomyKpiNameMappings?.reportDate ?? 'Report Date'"
                  :explanation="euTaxonomyKpiInfoMappings?.reportDate ?? 'Report Date'"
              />
              <div class="lg:col-6 md:col-6 col-12 p-0">
                <Calendar
                    data-test="reportDate"
                    inputId="icon"
                    v-model="formsDatesFilesToUpload[index]"
                    :showIcon="true"
                    dateFormat="D, M dd, yy"
                    @update:modelValue="updateReportDateHandler(index, $event, 'filesToUpload')"
                />
              </div>

              <FormKit type="text" :modelValue="filesToUpload[index].reportDate" name="reportDate" />
            </div>

            <FormKit type="text" :modelValue="filesToUpload[index].documentId" name="reference" />

            <!-- Currency used in the report -->
            <div class="form-field" data-test="currencyUsedInTheReport">
              <UploadFormHeader
                  :name="euTaxonomyKpiNameMappings?.currency ?? 'Currency'"
                  :explanation="euTaxonomyKpiInfoMappings?.currency ?? 'Currency'"
              />
              <div class="lg:col-4 md:col-4 col-12 p-0">
                <FormKit
                    type="text"
                    name="currency"
                    validation="required|length:2,3"
                    validation-label="Currency used in the report"
                    placeholder="Currency used in the report"
                />
              </div>
            </div>
            <!-- Integrated report is on a group level -->
            <div class="form-field">
              <YesNoComponent
                  :displayName="euTaxonomyKpiNameMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
                  :info="euTaxonomyKpiInfoMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
                  :name="'isGroupLevel'"
              />
            </div>
          </FormKit>
        </div>


      </div>
    </div>
    <div v-if="editMode" class="uploadFormSection">
      <!-- List of company reports -->
      <div class="col-3 p-3 topicLabel">
        <h4 id="uploadReports" class="anchor title">Uploaded company reports</h4>
      </div>
      <div v-for="(file, index) of uploadFiles" :key="file.name" class="col-9 formFields">
        <div class="form-field-label">
          <h3 class="mt-0">{{ file.name.split(".")[0] }}</h3>
          {{JSON.stringify(file)}}
        </div>
        <FormKit :name="file.name.split('.')[0]" type="group">
          <!-- Date of the report -->
          <div class="form-field">
            <UploadFormHeader
              :name="euTaxonomyKpiNameMappings?.reportDate ?? 'Report Date'"
              :explanation="euTaxonomyKpiInfoMappings?.reportDate ?? 'Report Date'"
            />
            <div class="lg:col-6 md:col-6 col-12 p-0">
              <Calendar
                data-test="reportDate"
                inputId="icon"
                :modelValue="uploadFiles[index].convertedReportDate"
                :showIcon="true"
                dateFormat="D, M dd, yy"
                @update:modelValue="updateReportDateHandler(index, $event, 'uploadFiles')"
              />
            </div>
{{uploadFiles[index].convertedReportDate}}
            <FormKit type="text" :modelValue="uploadFiles[index].reportDate" name="reportDate" />
          </div>

          <FormKit type="text" :modelValue="uploadFiles[index].reference" name="reference" />

          <!-- Currency used in the report -->
          <div class="form-field" data-test="currencyUsedInTheReport">
            <UploadFormHeader
              :name="euTaxonomyKpiNameMappings?.currency ?? 'Currency'"
              :explanation="euTaxonomyKpiInfoMappings?.currency ?? 'Currency'"
            />
            <div class="lg:col-4 md:col-4 col-12 p-0">
              <FormKit
                type="text"
                name="currency"
                validation="required|length:2,3"
                validation-label="Currency used in the report"
                placeholder="Currency used in the report"
              />
            </div>
          </div>
          <!-- Integrated report is on a group level -->
          <div class="form-field">
            <YesNoComponent
              :displayName="euTaxonomyKpiNameMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
              :info="euTaxonomyKpiInfoMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
              :name="'isGroupLevel'"
            />
          </div>
        </FormKit>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";

import Calendar from "primevue/calendar";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import PrimeButton from "primevue/button";
import FileUpload, { FileUploadEmits } from "primevue/fileupload";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import {ExtendedCompanyReport, WhichSetOfFiles} from "@/components/forms/Types";

export default defineComponent({
  name: "UploadReports",
  components: {
    Calendar,
    UploadFormHeader,
    PrimeButton,
    FileUpload,
    YesNoComponent,
  },
  emits: ["selectedFiles", "removeReportFromFilesToUpload", "updateReportDateHandler"],
  data() {
    return {
      formsDatesFilesToUpload: [] as string[] | undefined,
      formsDatesUploadedFiles: [] as string[] | undefined,
      formatBytesUserFriendly,
    }
  },
  watch: {
    uploadFiles(newValue) {
      console.log("qqqq", newValue);
    },
  },
  mounted() {
    this.reassignDate();
  },
  methods: {
    reassignDate() {
      console.log('eleleleleleclel', this.uploadFiles.slice())
      this.formsDatesUploadedFiles = this.uploadFiles.map((el) => {
        console.log('eleleleleleclel', el)
        return new Date(el.reportDate);
      })
    },
    /**
     * Function to emit event when files are selected
     *
     * @param event date in date format
     * @param event.originalEvent event
     * @param event.files files
     */
    onSelectedFiles(event: { files: Record<string, string>[]; originalEvent: Event }) {
      this.$emit("selectedFiles", event);
    },
    /**
     * Function to emit event when files are selected remove report from files to upload
     *
     * @param fileToRemove File To Remove
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param index Index number of the report
     */
    removeReportFromFilesToUpload(
      fileToRemove: Record<string, string>,
      fileRemoveCallback: (x: number) => void,
      index: number
    ) {
      this.$emit("removeReportFromFilesToUpload", fileToRemove, fileRemoveCallback, index);
    },
    /**
     * Function to emit event to update the date of a single report file
     *
     * @param index file to update
     * @param event new date value
     * @param whichSetOfFiles which set of files will be edited
     */
    updateReportDateHandler(index: number, event: Date, whichSetOfFiles: WhichSetOfFiles) {
      this.$emit("updateReportDateHandler", index, event, whichSetOfFiles);
    },
    /**
     * Function to clear all not uploaded files
     *
     */
    clearAllNotUploadedFiles(): void {
      (this.$refs.fileUpload as FileUploadEmits).clear();
    },
  },

  props: {
    filesToUpload: {
      type: Array,
    },
    uploadFiles: {
      type: Array,
    },
    editMode: {
      type: Boolean,
    },
    maxFileSize: {
      type: Number,
    },
    euTaxonomyKpiNameMappings: {},
    euTaxonomyKpiInfoMappings: {},
  },
});
</script>
