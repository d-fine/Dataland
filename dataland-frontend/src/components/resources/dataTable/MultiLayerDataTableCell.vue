<template>
  <template v-if="content.displayComponent == MLDTDisplayComponents.StringDisplayComponent">
    <span v-if="content.displayValue">{{ content.displayValue }}</span>
  </template>

  <template v-else-if="isDisplayValueOfType(MLDTDisplayComponents.DocumentLinkDisplayComponent, content)">
    <DocumentLink
      :label="content.displayValue.label"
      :download-name="content.displayValue.reference.name"
      :reference="content.displayValue.reference.reference"
      show-icon
    />
  </template>

  <template v-else-if="isDisplayValueOfType(MLDTDisplayComponents.ModalLinkDisplayComponent, content)">
    <a @click="dialog.open(content.displayValue.modalComponent, content.displayValue.modalOptions)" class="link"
      >{{ content.displayValue.label }}
      <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
    </a>
  </template>
</template>

<script generic="T extends MLDTDisplayComponents" setup lang="ts">
import {
  isDisplayValueOfType,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { useDialog } from "primevue/usedialog";

const dialog = useDialog();

defineProps<{
  content: MLDTDisplayValue<T>;
}>();
</script>

<style scoped></style>
