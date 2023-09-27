<template>
  <a v-if="validatedData()" @click="openModal()" class="link"
    >Show {{ linkLabel }}
    <em class="material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import type AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import type NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";

export type GenericsDataTableRequiredData =
  | {
      dataId: string;
      kpiLabel: string;
      kpiKey: string;
      content: { [dataId: string]: Array<object | string> };
      columnHeaders: Record<string, unknown>;
    }
  | undefined;

export default defineComponent({
  name: "GenericDataTableModalLink",
  props: {
    displayComponent: {
      type: Object as () =>
        | typeof DetailsCompanyDataTable
        | typeof AlignedActivitiesDataTable
        | typeof NonAlignedActivitiesDataTable,
      required: true,
    },
    data: {
      type: Object as () => GenericsDataTableRequiredData,
      required: true,
    },
  },
  data() {
    return {
      linkLabel: "",
      modalTitle: "",
    };
  },
  created() {
    if (this.data?.kpiLabel) {
      this.linkLabel = `"${this.data.kpiLabel}"`;
      this.modalTitle = this.data.kpiLabel;
    }
  },
  methods: {
    /**
     * Validate received data
     * @returns whether all necessary data is present
     */
    validatedData(): boolean {
      return (
        !!this.data && !!this.data.content && !!this.data.dataId && !!this.data.kpiLabel && !!this.data.columnHeaders
      );
    },
    /**
     * Opens the modal
     */
    openModal() {
      const dialogData = {
        listOfRowContents: this.data.content[this.data.dataId],
        kpiKeyOfTable: this.data.kpiKey,
        columnHeaders: this.data.columnHeaders,
      };

      this.$dialog.open(this.displayComponent, {
        props: {
          header: this.modalTitle,
          modal: true,
          dismissableMask: true,
        },
        data: dialogData,
      });
    },
  },
});
</script>
