<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Header"
    class="col-6"
    v-model:visible="internalDialogVisible"
    @hide="closeDialog"
  >
    <template #header>
      <h2 class="m-0">Document Details</h2>
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeDialog from 'primevue/dialog';
import { inject, onMounted, ref, watch } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import type { DocumentMetaInfoEntity } from '@clients/documentmanager';

const props = defineProps<{
  dialogVisible: boolean;
  documentId: string;
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const internalDialogVisible = ref(props.dialogVisible);
const emit = defineEmits(['update:dialogVisible']);
const metaData = ref<DocumentMetaInfoEntity | null>(null);

/**
 * Get metadata of document
 */
async function getDocumentMetaInformation(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
        .documentController;
      metaData.value = (await documentControllerApi.getDocumentMetaInformation(props.documentId)).data;
    }
  } catch (error) {
    console.error(error);
  }
}

watch(internalDialogVisible, (newValue) => {
  emit('update:dialogVisible', newValue);
});

watch(
  () => props.dialogVisible,
  (newValue) => {
    if (internalDialogVisible.value !== newValue) {
      internalDialogVisible.value = newValue;
    }
  }
);

const closeDialog = () => {
  internalDialogVisible.value = false;
};

onMounted(() => {
  getDocumentMetaInformation();
});
</script>

<style scoped lang="scss"></style>
