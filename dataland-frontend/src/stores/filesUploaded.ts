import { defineStore } from "pinia";

export const useFilesUploadedStore = defineStore("reportsFilesUploaded", {
  state: () => {
    return {
      files: [] as Record<string, string>[],
      filesNames: [] as Array<string>,
    };
  },
  actions: {
    setReportsFilesUploaded(fileToSet: Record<string, string>) {
      this.files = [...this.files, fileToSet];
      this.filesNames = this.files.map((el) => el.name);
    },
    removeReportFromFilesUploaded(
      fileToRemove: Record<string, string>,
      fileRemoveCallback: (x: number) => void,
      index: number
    ) {
      fileRemoveCallback(index);
      this.files = this.files.filter((el) => {
        return el.name !== fileToRemove.name;
      });
    },
    updatePropertyFilesUploaded(indexFileToUpload: number, property: string, value: string) {
      if (Object.hasOwn(this.files[indexFileToUpload], property)) {
        this.files[indexFileToUpload][property] = value;
        this.reRender();
      } else {
        return;
      }
    },
    reRender() {
      this.files = [...this.files];
    },
  },
});
