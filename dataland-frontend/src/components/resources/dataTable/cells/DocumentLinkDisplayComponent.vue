<template>
  <DocumentDownloadLink
    :document-download-info="{
      downloadName: content.displayValue.dataSource.fileName ?? content.displayValue.dataSource.fileReference,
      fileReference: content.displayValue.dataSource.fileReference,
    }"
    :label="content.displayValue.label"
    show-icon
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';

export default defineComponent({
  name: 'DocumentLinkDisplayComponent',
  components: { DocumentDownloadLink },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>,
      required: true,
    },
  },
});
</script>
