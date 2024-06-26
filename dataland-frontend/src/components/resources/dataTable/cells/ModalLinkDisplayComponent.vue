<template>
  <a @click="$dialog.open(content.displayValue.modalComponent, modalOptions)" class="link"
    >{{ content.displayValue.label }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type DataMetaInformation } from '@clients/backend';
export default defineComponent({
  name: 'ModalLinkDisplayComponent',
  computed: {
    modalOptions() {
      let updatedModalOptions = this.content.displayValue.modalOptions;
      updatedModalOptions!.data.metaInfo = this.metaInfo;
      return updatedModalOptions;
    },
  },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>,
      required: true,
    },
    metaInfo: {
      type: Object as () => DataMetaInformation,
      required: true,
    },
  },
});
</script>
