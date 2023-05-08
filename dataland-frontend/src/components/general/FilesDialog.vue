<template>
  <div>{{ message }}</div>
  <div v-for="(fileName, index) of listOfFileNames" :key="fileName + index">
    <br />
    <div>{{ fileName }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";

export default defineComponent({
  inject: ["dialogRef"],
  name: "FileDialog",
  data() {
    return {
      message: undefined as undefined | string,
      listOfFileNames: undefined as undefined | string[],
    };
  },

  mounted() {
    this.getDataFromParentAndSet();
  },

  methods: {
    /**
     * Gets all the data that is passed down by the component which has opened this modal and stores it in the
     * component-level data object.
     */
    getDataFromParentAndSet() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      const dialogRefData = dialogRefToDisplay.data as {
        message: string;
        listOfFileNames: string[];
      };
      this.message = dialogRefData.message;
      this.listOfFileNames = dialogRefData.listOfFileNames;
    },
  },
});
</script>
