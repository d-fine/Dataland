<template>
  <div data-test="download-link-component">
    <PrimeButton
      v-if="isUserLoggedIn"
      variant="text"
      @click="handleDocumentDownload()"
      :data-test="'download-link-' + documentDownloadInfo.downloadName"
    >
      <span>{{ label ?? documentDownloadInfo.downloadName }}</span>
      <span>{{ suffix ?? '' }}</span>
      <span>
        <i
          v-if="showIcon && (percentCompleted === 0 || percentCompleted === undefined)"
          class="pi pi-download"
          data-test="download-icon"
        ></i>
        <i
          v-else-if="showIcon && percentCompleted > 0 && percentCompleted < 100"
          class="pi pi-spin pi-spinner"
          data-test="spinner-icon"
          style="margin-left: var(--spacing-xs)"
        ></i>

        <span v-if="percentCompleted > 0 && percentCompleted < 100" data-test="percentage-text">
          ({{ percentCompleted }}%)
        </span>
      </span>
    </PrimeButton>
    <span
      v-else
      :class="fontStyle"
      :title="documentDownloadInfo.downloadName"
      :data-test="'download-text-' + documentDownloadInfo.downloadName"
    >
      <span
        style="overflow: hidden; text-overflow: ellipsis"
        :data-test="'Report-Download-' + documentDownloadInfo.downloadName"
        >{{ label ?? documentDownloadInfo.downloadName }}</span
      >
      <span>{{ suffix ?? '' }}</span>
    </span>
  </div>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref } from 'vue';
import type Keycloak from 'keycloak-js';

import {
  createNewPercentCompletedRef,
  downloadDocument,
  downloadIsInProgress,
  type DocumentDownloadInfo,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import PrimeButton from 'primevue/button';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = (createNewPercentCompletedRef() ?? ref(0)) as Ref<number>;

const isUserLoggedIn = ref<undefined | boolean>(undefined);

const props = defineProps({
  label: String,
  suffix: String,
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
  showIcon: Boolean,
  fontStyle: String,
});

onMounted(() => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      isUserLoggedIn.value = keycloak.authenticated;
    })
    .catch((error) => console.error(error));
});

const handleDocumentDownload = async (): Promise<void> => {
  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>

<style scoped>
div {
  white-space: nowrap;
  max-width: 100%;
}
</style>
