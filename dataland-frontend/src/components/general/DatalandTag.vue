<template>
  <Tag
    v-if="severity"
    class="dataland-tag"
    :dt="{ colorScheme: { light: { primary: designToken } } }"
    :pt="{
      root: {
        style: 'border: 1px solid color-mix(in srgb, var(--p-tag-primary-color) 50%, transparent);',
      },
    }"
    :value="value"
  />
</template>

<script setup lang="ts">
import { ExtendedQaStatus } from '@/components/resources/datasetOverview/DatasetTableInfo.ts';
import Tag from 'primevue/tag';
import { computed, type Ref } from 'vue';
import { RequestState, RequestPriority, DataSourcingState } from '@clients/datasourcingservice';

type TagColorDefinition = {
  background: string;
  color: string;
};

const stateAmberTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{amber.700}',
};

const stateBlueTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{blue.800}',
};

const stateGreenTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{green.700}',
};

const stateRedTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{red.700}',
};

const stateSlateTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{slate.400}',
};

const priorityLowTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{sky.600}',
};

const priorityBaselineTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{amber.600}',
};

const priorityHighTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{primary.color}',
};

const priorityUrgentTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{red.600}',
};

const qaAmberTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{amber.700}',
};

const qaGreenTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{green.700}',
};

const qaRedTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{red.700}',
};

const qaYellowTag: TagColorDefinition = {
  background: '{surface.0}',
  color: '{yellow.800}',
};

const { severity, value } = defineProps({
  severity: {
    type: String,
    required: true,
  },
  value: {
    type: String,
    required: false,
    default: undefined,
  },
});

const designToken: Ref<TagColorDefinition> = computed(() => {
  switch (severity) {
    case ExtendedQaStatus.Pending:
      return qaAmberTag;
    case ExtendedQaStatus.Accepted:
      return qaGreenTag;
    case ExtendedQaStatus.Rejected:
      return qaRedTag;
    case ExtendedQaStatus.Superseded:
      return qaYellowTag;
    case RequestPriority.Low:
      return priorityLowTag;
    case RequestPriority.Baseline:
      return priorityBaselineTag;
    case RequestPriority.High:
      return priorityHighTag;
    case RequestPriority.Urgent:
      return priorityUrgentTag;
    case RequestState.Open:
      return stateAmberTag;
    case RequestState.Processing:
      return stateBlueTag;
    case RequestState.Processed:
      return stateGreenTag;
    case RequestState.Withdrawn:
      return stateSlateTag;
    case DataSourcingState.Initialized:
      return stateAmberTag;
    case DataSourcingState.DocumentSourcing:
    case DataSourcingState.DocumentSourcingDone:
    case DataSourcingState.DataExtraction:
    case DataSourcingState.DataVerification:
      return stateBlueTag;
    case DataSourcingState.Done:
      return stateGreenTag;
    case DataSourcingState.NonSourceable:
      return stateRedTag;
    default:
      return stateSlateTag;
  }
});
</script>

<style scoped lang="scss">
.dataland-tag {
  height: 1.75rem;
  padding: 0 0.625rem;
  font-size: 0.875rem;
  font-weight: 400;
  white-space: nowrap;
  vertical-align: middle;
  border-radius: 4px;
}
</style>
