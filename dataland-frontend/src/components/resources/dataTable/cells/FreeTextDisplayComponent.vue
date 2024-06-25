<template>
  <span data-test="freetext-full" v-if="expanded || !teaserText" class="preserve-line-wrap">{{
    content.displayValue
  }}</span>
  <span data-test="freetext-collapsed" v-else class="preserve-line-wrap">{{ teaserText }}</span>
  <span data-test="freetext-toggle" v-if="teaserText" @click="expanded = !expanded" class="link">{{ toggleText }}</span>
</template>

<style scoped>
.preserve-line-wrap {
  white-space: pre-wrap;
}
</style>

<script lang="ts">
import { defineComponent } from 'vue';
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

const CUTOFF_CHARACTER_COUNT = 200;

export default defineComponent({
  name: 'FreeTextDisplayComponent',
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.FreeTextDisplayComponent>,
      required: true,
    },
  },
  data() {
    return {
      expanded: false,
    };
  },
  computed: {
    teaserText(): string | null {
      if (this.content.displayValue.length <= CUTOFF_CHARACTER_COUNT) return null;

      return this.content.displayValue.slice(0, CUTOFF_CHARACTER_COUNT);
    },
    toggleText(): string {
      return this.expanded ? 'Show less' : 'Show more';
    },
  },
});
</script>
