<template>
  <div :class="`p-badge badge-${assertDefined(statusMap.get(datasetStatus)).color}`">
    <span>{{ assertDefined(statusMap.get(datasetStatus)).text }}</span>
  </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from "vue";
import { DatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "DatasetStatusBadge",
  data() {
    return {
      assertDefined,
      statusMap: new Map<DatasetStatus, BadgeProperties>([
        [DatasetStatus.QAApproved, BadgeProperties.Approved],
        [DatasetStatus.QAPending, BadgeProperties.Pending],
        [DatasetStatus.Superseded, BadgeProperties.Superseded],
      ]),
    };
  },
  props: {
    datasetStatus: {
      type: Number as PropType<DatasetStatus>,
      required: true,
    },
  },
});

class BadgeProperties {
  static readonly Approved = new BadgeProperties("green", "APPROVED");
  static readonly Pending = new BadgeProperties("yellow", "PENDING");
  static readonly Superseded = new BadgeProperties("brown", "SUPERSEDED");

  readonly color: string;
  readonly text: string;

  constructor(color: string, text: string) {
    this.color = color;
    this.text = text;
  }
}
</script>
