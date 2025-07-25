<template>
  <Tag
    :severity="severity"
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
import { AccessStatus, RequestPriority, RequestStatus } from '@clients/communitymanager';
import Tag from 'primevue/tag';
import { computed, type Ref } from 'vue';

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
    // access status
    case AccessStatus.Declined:
      return yellowTag;
    case AccessStatus.Granted:
      return greenTag;
    case AccessStatus.Pending:
      return amberTag;
    case AccessStatus.Public:
      return skyTag;
    case AccessStatus.Revoked:
      return slateTag;
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
    // request status
    case RequestStatus.Open:
      return amberTag;
    case RequestStatus.Answered:
      return skyTag;
    case RequestStatus.Closed:
      return yellowTag;
    case RequestStatus.NonSourceable:
      return slateTag;
    case RequestStatus.Resolved:
      return greenTag;
    case RequestStatus.Withdrawn:
      return slateTag;
    default:
      throw TypeError('Invalid input for field "severity".');
  }
});
</script>
