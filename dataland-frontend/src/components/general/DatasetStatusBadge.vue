<template>
  <div :class="`p-badge badge-${assertDefined(statusMap.get(datasetStatus)).color}`">
    <span data-test="qa-status">{{ assertDefined(statusMap.get(datasetStatus)).text }}</span>
  </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue';
import { DatasetStatus } from '@/components/resources/datasetOverview/DatasetTableInfo';
import { assertDefined } from '@/utils/TypeScriptUtils';

export default defineComponent({
  name: 'DatasetStatusBadge',
  data() {
    return {
      assertDefined,
      statusMap: new Map<DatasetStatus, BadgeProperties>([
        [DatasetStatus.QaApproved, BadgeProperties.Approved],
        [DatasetStatus.QaPending, BadgeProperties.Pending],
        [DatasetStatus.QaRejected, BadgeProperties.Rejected],
        [DatasetStatus.Superseded, BadgeProperties.Superseded],
      ]),
    };
  },
  props: {
    datasetStatus: {
      type: Number,
      required: true,
    },
  },
});

class BadgeProperties {
  static readonly Approved = new BadgeProperties('green', 'APPROVED');
  static readonly Pending = new BadgeProperties('yellow', 'PENDING');
  static readonly Rejected = new BadgeProperties('red', 'REJECTED');
  static readonly Superseded = new BadgeProperties('brown', 'SUPERSEDED');

  readonly color: string;
  readonly text: string;

  constructor(color: string, text: string) {
    this.color = color;
    this.text = text;
  }
}
</script>
