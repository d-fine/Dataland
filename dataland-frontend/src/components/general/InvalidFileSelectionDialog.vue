<template>
  <div>The following file(s) cannot be selected.</div>
  <br />
  <div v-if="this.duplicateNamesJoinedString !== ''">
    <p>Files with duplicate names:</p>
    <strong>{{ this.duplicateNamesJoinedString }}</strong>
  </div>
  <br />
  <div v-if="this.fileNamesWithCharacterViolationsJoinedString !== ''">
    <p>Files with characters that are not accepted:</p>
    <strong>{{ this.fileNamesWithCharacterViolationsJoinedString }}</strong>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";

export default defineComponent({
  inject: ["dialogRef"],
  name: "InvalidFileSelectionDialog",
  data() {
    return {
      duplicateNamesJoinedString: "",
      fileNamesWithCharacterViolationsJoinedString: "",
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
        duplicateNamesJoinedString: string;
        fileNamesWithCharacterViolationsJoinedString: string;
      };
      this.duplicateNamesJoinedString = dialogRefData.duplicateNamesJoinedString;
      this.fileNamesWithCharacterViolationsJoinedString = dialogRefData.fileNamesWithCharacterViolationsJoinedString;
    },
  },
});
</script>
