import { defineStore } from "pinia";

export const useFilesUploadedStore = defineStore("reportsFilesUploaded", {
  state: () => {
    return {
      filesCtx: [] as Record<string, string>[],
      filesNames: [] as Array<string>,
    };
  },
  actions: {
    setReportsFilesUploaded(filesToSet: []) {
      console.log('wczytuje', filesToSet)
      this.filesCtx = Array.from(new Set([...this.filesCtx, ...filesToSet]));
      this.filesNames = this.filesCtx.map((el) => el.name);
    },
    removeReportFromFilesUploaded(
      fileToRemove: Record<string, string>,
      fileRemoveCallback: (x: number) => void,
      index: number
    ) {
      fileRemoveCallback(index);
      this.filesCtx = this.filesCtx.filter((el) => {
        return el.name !== fileToRemove.name;
      });
      this.filesNames = this.filesNames.filter((el) => {
        return el !== fileToRemove.name;
      });
    },
    updatePropertyFilesUploaded(indexFileToUpload: number, property: string, value: string) {
      console.log('property', property)
      console.log('property', value)
      if (Object.hasOwn(this.filesCtx[indexFileToUpload], property)) {
        this.filesCtx[indexFileToUpload][property] = value;
      } else {
        return;
      }
      this.reRender();
      console.log('this.filesCtx[indexFileToUpload][property]', this.filesCtx)
    },
    reRender() {
      this.filesCtx = [...this.filesCtx];
    },
    clearFiles() {
      this.filesCtx = [];
      this.filesNames = [];
    },
  },
});
