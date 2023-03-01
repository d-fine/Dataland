import { defineStore } from "pinia";

export const useFilesUploadedStore = defineStore("reportsFilesUploaded", {
  state: () => {
    return {
      files: [],
      filesNames: [],
    };
  },
  actions: {
    setReportsFilesUploaded(fileToSet) {
      this.files = [...this.files, fileToSet];
      this.filesNames = this.files.map(el => el.name)
    },
    removeReportFromFilesUploaded(fileToRemove) {
      this.files = this.files.filter((el) => {
        return el.name !== fileToRemove.name;
      });
    },
    updatePropertyFilesUploaded(indexFileToUpload, property, value) {
      // this.files.splice(indexFileToUpload, 1, {...this.files[indexFileToUpload], property: value})
      this.files[indexFileToUpload][property] = value;
    },
    reRender() {
      this.files = [...this.files]
    }
  },
});
