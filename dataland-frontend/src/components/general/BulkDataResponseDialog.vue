<template>
  <div data-test="bulkDataResponseDialog">
    <p>Edit or remove them.</p>

    <div class="flex flex-direction-column">
      <RemoveAddSingleItemElement
        v-for="identifier in responseData.acceptedCompanyIdentifiers"
        :key="identifier"
        :label="identifier"
        :value="identifier"
        @removed="onIdentifierChangeHandler('removed', $event)"
        @undo="onIdentifierChangeHandler('undo', $event)"
      >
        <template v-slot:default>
          <div class="validation-alert validation-alert-success mt-2 mb-4">
            <em class="material-icons info-icon green-text">check_circle</em>
            Identifier is valid
          </div>
        </template>
        <template v-slot:removed>
          <div class="validation-alert validation-alert-success mt-2 mb-4">
            <em class="material-icons info-icon green-text">check_circle</em>
            Identifier is removed
          </div>
        </template>
      </RemoveAddSingleItemElement>

      <RemoveAddSingleItemElement
        v-for="identifier in responseData.rejectedCompanyIdentifiers"
        :key="identifier"
        :label="identifier"
        :value="identifier"
        @removed="onIdentifierChangeHandler('removed', $event)"
        @undo="onIdentifierChangeHandler('undo', $event)"
      >
        <template v-slot:default>
          <div class="validation-alert validation-alert-error mt-2 mb-4">
            <em class="material-icons info-icon red-text">error</em>
            Identifier not matched to company
          </div>
        </template>
        <template v-slot:removed>
          <div class="validation-alert validation-alert-success mt-2 mb-4">
            <em class="material-icons info-icon red-text">error</em>
            Identifier is removed
          </div>
        </template>
      </RemoveAddSingleItemElement>
    </div>
  </div>
</template>

<script lang="ts">
import { type BulkDataRequestResponse } from "@clients/communitymanager";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { defineComponent } from "vue";
import RemoveAddSingleItemElement from "@/components/general/RemoveAddSingleItemElement.vue";

export default defineComponent({
  inject: ["dialogRef"],
  components: {
    RemoveAddSingleItemElement,
  },
  name: "BulkDataResponseDialog",

  data() {
    return {
      onItemChangeHandler: undefined as unknown as (event: string, identifier: string) => void | undefined,
      responseData: {},
    };
  },

  mounted() {
    this.getDataFromParentAndSet();
  },

  methods: {
    /**
     * Sends identifier back to parent for removal
     * @param eventName which emit event
     * @param identifier value to be removed from identifiers
     */
    onIdentifierChangeHandler(eventName: "removed" | "undo", identifier: string) {
      this.onItemChangeHandler && this.onItemChangeHandler(eventName, identifier);
    },
    /**
     * Gets all the data that is passed down by the component which has opened this modal and stores it in the
     * component-level data object.
     */
    getDataFromParentAndSet() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      const dialogRefData = dialogRefToDisplay.data as {
        onItemChangeHandler: (identifier: string) => void;
        responseData: BulkDataRequestResponse;
      };
      this.onItemChangeHandler = dialogRefData.onItemChangeHandler;
      this.responseData = dialogRefData.responseData;
    },
  },
});
</script>

<style scoped lang="scss">
.identifier-wrapper {
  font-family: monospace;
  background: var(--gray-100);
  padding: 1rem;
}

.validation-alert {
  font-size: 0.7rem;
  font-weight: 500;
  letter-spacing: 0.4px;

  .info-icon {
    font-size: 1rem;
    vertical-align: bottom;
  }

  &.validation-alert-error {
    color: var(--fk-color-error);
    border-top: var(--fk-color-error);
  }
}
</style>
