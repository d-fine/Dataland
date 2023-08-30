<template>
  <a v-if="validatedData()" @click="openModal()" class="link"
    >Show {{ linkLabel }}
    <em class="material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";

export type DetailsCompanyDataTableRequiredData =
  | {
      dataId: string;
      kpiLabel: string;
      kpiKey: string;
      content: { [dataId: string]: Array<object | string> };
      columnHeaders: Record<string, unknown>;
    }
  | undefined;

export type DetailsCompanyDataTablePreparedData = {
  listOfRowContents: Array<object | string>;
  kpiKeyOfTable: string;
  columnHeaders: Record<string, unknown>;
};

export default defineComponent({
  name: "DetailsCompanyDataTableModal",
  components: { DetailsCompanyDataTable },
  props: {
    data: {
      type: Object as () => DetailsCompanyDataTableRequiredData,
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
      if (this.data) {
        const dialogData = {
          listOfRowContents: this.data.content[this.data.dataId],
          kpiKeyOfTable: this.data.kpiKey,
          columnHeaders: this.data.columnHeaders,
        };

        if (dialogData) {
          this.$dialog.open(DetailsCompanyDataTable, {
            props: {
              header: this.modalTitle,
              modal: true,
              dismissableMask: true,
            },
            data: dialogData,
          });
        }
      }
    },
  },
});
</script>
