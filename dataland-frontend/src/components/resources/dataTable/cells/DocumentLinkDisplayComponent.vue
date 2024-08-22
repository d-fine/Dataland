<template>
  <DocumentLink
    :label="content.displayValue.label"
    :download-name="content.displayValue.dataSource.fileName ?? content.displayValue.dataSource.fileReference"
    :file-reference="content.displayValue.dataSource.fileReference"
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
