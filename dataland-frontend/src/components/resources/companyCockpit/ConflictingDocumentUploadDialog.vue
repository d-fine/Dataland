<template>
  <PrimeDialog
    v-model:visible="isVisible"
    modal
    @show="handleUploadConflict"
    :closable="false"
    :dismissableMask="true"
    style="border-radius: 0.75rem; text-align: center"
    :show-header="false"
    data-test="conflictModal"
  >
    <div style="margin: 10px">
      <h2 class="m-0" data-test="conflictText">Document already exists</h2>
    </div>
    <div v-if="isLoading" class="d-center-div text-center px-7 py-4">
      <DatalandProgressSpinner />
    </div>
    <div v-else>
      <div v-if="isConflictForOwnCompany">
        <div class="text-block" style="margin: 15px; white-space: pre">
          The document is already associated with this company.
        </div>
        <div>
          <Button label="Ok" class="p-button-text" @click="onCancel" data-test="ok-button" />
        </div>
      </div>
      <div v-else>
        <div class="text-block" style="margin: 15px; white-space: pre">
          The document already exists for the following companies:
          <ul>
            <li v-for="companyName in conflictingCompanyNames" :key="companyName">{{ companyName }}</li>
          </ul>
          Do you also want to associate this document with {{ ownCompanyName }}?
        </div>
        <div class="button-row">
          <Button label="Cancel" class="p-button-text" @click="onCancel" data-test="cancel-button" />
          <Button
            label="Associate Document"
            class="p-button"
            @click="handleAssociateDocument"
            data-test="associate-document-button"
          />
        </div>
      </div>
    </div>
  </PrimeDialog>
  <PrimeDialog
    id="successModal"
    v-model:visible="successModalIsVisible"
    modal
    :closable="false"
    :dismissableMask="true"
    style="border-radius: 0.75rem; text-align: center"
    :show-header="false"
    @hide="closeSuccessModal"
    data-test="success-modal"
  >
    <div class="text-center" style="display: flex; flex-direction: column">
      <div style="margin: 10px">
        <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
      </div>
      <div style="margin: 10px">
        <h2 class="m-0" data-test="successText">Success</h2>
      </div>
    </div>
    <div class="text-block" style="margin: 15px; white-space: pre">Document associated successfully.</div>
    <Button label="CLOSE" @click="closeSuccessModal" variant="outlined" data-test="close-success-modal-button" />
  </PrimeDialog>
</template>

<script lang="ts" setup>
import { inject, ref } from 'vue';
import PrimeDialog from 'primevue/dialog';
import Button from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { type DocumentMetaInfoPatch } from '@clients/documentmanager';

const props = defineProps<{ documentId: string; companyId: string }>();
const emit = defineEmits(['close', 'document-associated']);

const isVisible = ref<boolean>(true);
const isLoading = ref<boolean>(true);
const isConflictForOwnCompany = ref<boolean>(false);
const ownCompanyName = ref<string>('');
const conflictingCompanyIds = ref<Set<string>>(new Set());
const conflictingCompanyNames = ref<Set<string>>(new Set());
const successModalIsVisible = ref<boolean>(false);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const documentControllerApi = apiClientProvider.apiClients.documentController;
const companyControllerApi = apiClientProvider.backendClients.companyDataController;

/** Fetches the names of companies that have a conflict with the document being uploaded.
 * Populates the conflictingCompanyNames set with the names of these companies.
 */
async function getConflictingCompanyNames(): Promise<void> {
  try {
    const response = await documentControllerApi.getDocumentMetaInformation(props.documentId);
    conflictingCompanyIds.value = new Set(response.data.companyIds);
    for (const companyId of conflictingCompanyIds.value) {
      const companyName = (await companyControllerApi.getCompanyInfo(companyId)).data.companyName;
      conflictingCompanyNames.value.add(companyName);
    }
  } catch (error) {
    console.log('Error fetching conflicting company names:', error);
  }
}

/** Fetches the name of the current company based on the provided companyId prop.
 * Updates the ownCompanyName ref with the fetched company name.
 */
async function getOwnCompanyName(): Promise<void> {
  try {
    const companyInfo = await companyControllerApi.getCompanyInfo(props.companyId);
    ownCompanyName.value = companyInfo.data.companyName;
  } catch (error) {
    console.log('Error fetching own company name:', error);
  }
}

/** Handles the upload conflict by checking if the document is already associated with the current company.
 * Updates the isConflictForOwnCompany flag based on the presence of the current company ID in the conflictingCompanyIds set.
 */
async function handleUploadConflict(): Promise<void> {
  await getOwnCompanyName();
  await getConflictingCompanyNames();
  isConflictForOwnCompany.value = conflictingCompanyIds.value.has(props.companyId);
  isLoading.value = false;
}

/** Associates the document with the current company by updating its metadata.
 * If successful, shows a success modal.
 */
async function handleAssociateDocument(): Promise<void> {
  try {
    const companyIdsArray = Array.from(conflictingCompanyIds.value);
    if (!companyIdsArray.includes(props.companyId)) {
      companyIdsArray.push(props.companyId);
    }
    const documentMetaInfoPatch: DocumentMetaInfoPatch = {
      companyIds: companyIdsArray as unknown as Set<string>,
    };
    await documentControllerApi.patchDocumentMetaInfo(props.documentId, documentMetaInfoPatch);
    successModalIsVisible.value = true;
  } catch (error) {
    console.error('Error associating document with company:', error);
  }
}

/** Emits a close event to close the dialog. */
function onCancel(): void {
  isVisible.value = false;
  emit('close');
}

/** Closes the success modal.
 */
const closeSuccessModal = (): void => {
  successModalIsVisible.value = false;
  emit('close');
  emit('document-associated');
};
</script>

<style scoped lang="scss">
.text-block {
  text-align: left;
}
.button-row {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 1.5rem;
}

.green-text {
  color: var(--green);
}
</style>
