<template>
  <div v-if="this.duplicateNamesJoinedString !== ''">
    <p>Files with duplicate names:</p>
    <strong>{{ this.duplicateNamesJoinedString }}</strong>
  </div>
  <br v-if="this.duplicateNamesJoinedString !== '' && this.fileNamesWithCharacterViolationsJoinedString !== ''" />
  <div v-if="this.fileNamesWithCharacterViolationsJoinedString !== ''">
    <p>File names containing illegal characters:</p>
    <strong>{{ this.fileNamesWithCharacterViolationsJoinedString }}</strong>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

export default defineComponent({
  inject: ['dialogRef'],
  name: 'InvalidFileSelectionDialog',
  data() {
    return {
      duplicateNamesJoinedString: '',
      fileNamesWithCharacterViolationsJoinedString: '',
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
        duplicateNamesJoinedString: string;
        fileNamesWithCharacterViolationsJoinedString: string;
      };
      this.duplicateNamesJoinedString = dialogRefData.duplicateNamesJoinedString;
      this.fileNamesWithCharacterViolationsJoinedString = dialogRefData.fileNamesWithCharacterViolationsJoinedString;
    },
  },
});
</script>
