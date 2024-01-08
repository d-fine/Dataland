<template>
  <FormKit v-if="hasValidDataSource()" type="group" name="dataSource">
    <FormKit type="hidden" name="fileName" :modelValue="fileName" />
    <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
    <FormKit type="hidden" name="page" :modelValue="fileReference" />
  </FormKit>
</template>

<script lang="ts">
import { getFileReferenceByFileName } from "@/utils/FileUploadUtils";
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "DataSourceElement",
  components: { FormKit },
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
  },
  props: {
    pageForFileReference: {
      type: String,
      default: () => undefined,
    },
    currentReportValue: {
      type: String,
      default: () => undefined,
    },
  },
  data() {
    return {
      isMounted: false,
      noReportLabel: "None...",
    };
  },
  mounted() {
    setTimeout(() => (this.isMounted = true));
  },
  computed: {
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(this.currentReportValue as string, this.injectReportsNameAndReferences);
    },
    fileName(): string {
      return this.currentReportValue ?? this.noReportLabel;
    },
    fileReference(): string {
      return this.pageForFileReference ?? "";
    },
  },
  methods: {
    /**
     * Checks whether the Assurance data source has appropriate values
     * @returns if no file selected or 'None...' selected it returns undefined. Else it returns the data source
     */
    hasValidDataSource(): boolean {
      if (!this.isMounted) {
        return true;
      }
      return this.fileName?.length > 0 && this.fileName !== this.noReportLabel;
    },
  },
});
</script>
