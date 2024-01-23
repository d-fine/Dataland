<template>
  <div data-test="bulkDataResponseDialog">
    <p>Edit or remove them.</p>

    <div class="flex flex-direction-column">
      <RemoveSingleItemElement
        v-for="identifier in responseData.acceptedCompanyIdentifiers"
        :key="identifier"
        :label="identifier"
      ></RemoveSingleItemElement>

      <tamplate v-for="identifier in responseData.rejectedCompanyIdentifiers" :key="identifier">
        <RemoveSingleItemElement :label="identifier" />
      </tamplate>
    </div>
  </div>
  <!-- <pre wrap>{{ responseData }}</pre>
  <pre wrap>{{ bulkDataRequestModel }}</pre> -->
</template>

<script lang="ts">
import { type BulkDataRequestResponse } from "@clients/communitymanager";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { defineComponent } from "vue";
import RemoveSingleItemElement from "@/components/general/RemoveSingleItemElement.vue";

export default defineComponent({
  inject: ["dialogRef"],
  components: {
    RemoveSingleItemElement,
  },
  name: "BulkDataResponseDialog",

  data() {
    return {
      responseData: {},
      bulkDataRequestModel: {},
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
        responseData: BulkDataRequestResponse;
        bulkDataRequestModel: object;
      };
      this.responseData = dialogRefData.responseData;
      this.bulkDataRequestModel = dialogRefData.bulkDataRequestModel;
    },
  },
});
</script>

<style scoped lang="scss">
.identifier-wrapper {
  font-family: monospace;
  background: var(--gray-100);
  padding: 1rem;
  margin-bottom: 1.5rem;
}
</style>
