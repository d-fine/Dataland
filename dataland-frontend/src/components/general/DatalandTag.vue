<template>
  <Tag
    v-if="severity"
    :dt="{ colorScheme: { light: { primary: designToken } } }"
    :pt="{
      root: {
        style: 'border: 1px solid var(--p-tag-primary-color);',
      },
    }"
  />
</template>

<script setup lang="ts">
import { ExtendedQaStatus } from '@/components/resources/datasetOverview/DatasetTableInfo.ts';
import Tag from 'primevue/tag';
import { computed, type Ref } from 'vue';
import { RequestState, RequestPriority } from '@clients/datasourcingservice';

type TagColorDefinition = {
  background: string;
  color: string;
};

// Define tag color constants
const yellowTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{yellow.800}',
};

const greenTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{green.600}',
};

const amberTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{amber.600}',
};

const skyTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{sky.600}',
};

const slateTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{slate.600}',
};

const redTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{red.600}',
};

const primaryTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{primary.color}',
};

const { severity } = defineProps({
  severity: {
    type: String,
    required: true,
  },
});

const designToken: Ref<TagColorDefinition> = computed(() => {
  switch (severity) {
    // quality status
    case ExtendedQaStatus.Pending:
      return amberTag;
    case ExtendedQaStatus.Accepted:
      return greenTag;
    case ExtendedQaStatus.Rejected:
      return redTag;
    case ExtendedQaStatus.Superseded:
      return yellowTag;
    // request priority
    case RequestPriority.Low:
      return skyTag;
    case RequestPriority.Baseline:
      return amberTag;
    case RequestPriority.High:
      return primaryTag;
    case RequestPriority.Urgent:
      return redTag;
    // request state
    case RequestState.Open:
      return amberTag;
    case RequestState.Processing:
      return skyTag;
    case RequestState.Processed:
      return greenTag;
    case RequestState.Withdrawn:
      return slateTag;
    default:
      return slateTag;
  }
});
</script>
