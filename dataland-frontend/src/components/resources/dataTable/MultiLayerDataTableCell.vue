<template>
  <div class="flex" v-if="content.displayComponentName == MLDTDisplayComponents.HighlightHiddenCellDisplay">
    <i class="pi pi-eye-slash pr-1 text-red-500" aria-hidden="true" data-test="hidden-icon" v-if="inReviewMode" />
    <MultiLayerDataTableCell
      :content="content.displayValue.innerContents"
      :metaInfo="metaInfo"
      :inReviewMode="inReviewMode"
    />
  </div>
  <div v-if="content.displayComponentName == MLDTDisplayComponents.DataPointWrapperDisplayComponent">
    <DataPointWrapperDisplayComponent :content="content" :metaInfo="metaInfo">
      <MultiLayerDataTableCell
        :content="content.displayValue.innerContents"
        :meta-info="metaInfo"
        v-if="!hasBlankInnerContents"
        :inReviewMode="inReviewMode"
      />
      <template v-else>No data provided</template>
    </DataPointWrapperDisplayComponent>
  </div>
  <component v-else :is="content.displayComponentName" :content="content" :meta-Info="metaInfo" />
</template>

<script lang="ts">
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DocumentLinkDisplayComponent from '@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue';
import StringDisplayComponent from '@/components/resources/dataTable/cells/StringDisplayComponent.vue';
import { defineComponent } from 'vue';
import ModalLinkDisplayComponent from '@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue';
import ModalLinkWithDataSourceDisplayComponent from '@/components/resources/dataTable/cells/ModalLinkWithDataSourceDisplayComponent.vue';
import DataPointDisplayComponent from '@/components/resources/dataTable/cells/DataPointDisplayComponent.vue';
import DataPointWrapperDisplayComponent from '@/components/resources/dataTable/cells/DataPointWrapperDisplayComponent.vue';
import FreeTextDisplayComponent from '@/components/resources/dataTable/cells/FreeTextDisplayComponent.vue';
import { type DataMetaInformation } from '@clients/backend';

export default defineComponent({
  name: 'MultiLayerDataTableCell',
  computed: {
    MLDTDisplayComponents() {
      return MLDTDisplayComponentName;
    },
    hasBlankInnerContents(): boolean {
      if (this.content.displayComponentName == MLDTDisplayComponentName.DataPointWrapperDisplayComponent) {
        const innerContents = this.content.displayValue.innerContents;
        if (innerContents.displayComponentName == MLDTDisplayComponentName.StringDisplayComponent) {
          const innerLength = innerContents.displayValue?.length ?? 0;
          return innerLength <= 0;
        }
      }
      return false;
    },
  },
  components: {
    DataPointWrapperDisplayComponent,
    StringDisplayComponent,
    DocumentLinkDisplayComponent,
    ModalLinkDisplayComponent,
    DataPointDisplayComponent,
    FreeTextDisplayComponent,
    ModalLinkWithDataSourceDisplayComponent
  },
  props: {
    content: {
      type: Object as () => AvailableMLDTDisplayObjectTypes,
      required: true,
    },
    metaInfo: {
      type: Object as () => DataMetaInformation,
      required: true,
    },
    inReviewMode: {
      type: Boolean,
      required: true,
    },
  },
});
</script>
