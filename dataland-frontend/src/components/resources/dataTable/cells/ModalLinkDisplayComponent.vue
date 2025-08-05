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
      const updatedModalOptions = this.content.displayValue.modalOptions;
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
<style scoped>
.link {
  color: var(--main-color);
  background: transparent;
  border: transparent;
  cursor: pointer;
  display: flex;

  &:hover {
    color: hsl(from var(--main-color) h s calc(l - 20));
    text-decoration: underline;
  }

  &:active {
    color: hsl(from var(--main-color) h s calc(l + 10));
  }

  &:focus {
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}
</style>
