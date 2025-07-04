<template>
  <PrimeButton
    class="uppercase p-button p-button-sm d-letters ml-3"
    aria-label="download document"
    @click="handleDocumentDownload"
    data-test="document-download-button"
  >
    <DownloadProgressSpinner
      v-if="downloadIsInProgress(percentCompleted)"
      :percent-completed="percentCompleted"
      :white-spinner="true"
    />
    <span class="px-2 py-1" v-else>DOWNLOAD</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';

import {
  createNewPercentCompletedRef,
  downloadDocument,
  downloadIsInProgress,
  type DocumentDownloadInfo,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import PrimeButton from 'primevue/button';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = createNewPercentCompletedRef();

const props = defineProps({
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
});

const handleDocumentDownload = async (): Promise<void> => {
  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}
</style>
