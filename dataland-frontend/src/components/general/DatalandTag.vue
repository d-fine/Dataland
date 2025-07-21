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
      return {
        background: '{surface.0}',
        color: '{yellow.800}',
      };
    case AccessStatus.Granted:
      return {
        background: '{surface.0}',
        color: '{green.600}',
      };
    case AccessStatus.Pending:
      return {
        background: '{surface.0}',
        color: '{amber.600}',
      };
    case AccessStatus.Public:
      return {
        background: '{surface.0}',
        color: '{sky.600}',
      };
    case AccessStatus.Revoked:
      return {
        background: '{surface.0}',
        color: '{slate.600}',
      };
    // quality status
    case ExtendedQaStatus.Pending:
      return {
        background: '{surface.0}',
        color: '{amber.600}',
      };
    case ExtendedQaStatus.Accepted:
      return {
        background: '{surface.0}',
        color: '{green.600}',
      };
    case ExtendedQaStatus.Rejected:
      return {
        background: '{surface.0}',
        color: '{red.600}',
      };
    case ExtendedQaStatus.Superseded:
      return {
        background: '{surface.0}',
        color: '{yellow.800}',
      };
    // request priority
    case RequestPriority.Low:
      return {
        background: '{surface.0}',
        color: '{sky.600}',
      };
    case RequestPriority.Baseline:
      return {
        background: '{surface.0}',
        color: '{amber.600}',
      };
    case RequestPriority.High:
      return {
        background: '{surface.0}',
        color: '{primary.color}',
      };
    case RequestPriority.Urgent:
      return {
        background: '{surface.0}',
        color: '{red.600}',
      };
    // request status
    case RequestStatus.Open:
      return {
        background: '{surface.0}',
        color: '{amber.600}',
      };
    case RequestStatus.Answered:
      return {
        background: '{surface.0}',
        color: '{sky.600}',
      };
    case RequestStatus.Closed:
      return {
        background: '{surface.0}',
        color: '{yellow.800}',
      };
    case RequestStatus.NonSourceable:
      return {
        background: '{surface.0}',
        color: '{slate.600}',
      };
    case RequestStatus.Resolved:
      return {
        background: '{surface.0}',
        color: '{green.600}',
      };
    case RequestStatus.Withdrawn:
      return {
        background: '{surface.0}',
        color: '{slate.600}',
      };
    default:
      throw TypeError('Invalid input for field "severity".');
  }
});
</script>

<style scoped></style>
