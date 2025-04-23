<template>
  <DocumentLink
    :document-download-info="{
      downloadName: content.displayValue.dataSource.fileName ?? content.displayValue.dataSource.fileReference,
      fileReference: content.displayValue.dataSource.fileReference,
      page: undefined,
      dataId: undefined,
      dataType: undefined,
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
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';

export default defineComponent({
  name: 'DocumentLinkDisplayComponent',
  components: { DocumentLink },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>,
      required: true,
    },
  },
});
</script>
