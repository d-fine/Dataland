<template>
  <div>{{ message }}</div>
  <br />
  <div v-for="(name, index) of listOfElementNames" :key="name + index">
    <strong>{{ name }}</strong>
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
      listOfElementNames: undefined as undefined | string[],
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
        listOfElementNames: string[];
      };
      this.message = dialogRefData.message;
      this.listOfElementNames = dialogRefData.listOfElementNames;
    },
  },
});
</script>
