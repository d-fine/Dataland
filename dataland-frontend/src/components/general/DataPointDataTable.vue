<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr
            v-if="
              dialogData.dataPointDisplay.value && dialogData.dataPointDisplay.value != ONLY_AUXILIARY_DATA_PROVIDED
            "
          >
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Value</span></th>
            <td>{{ dialogData.dataPointDisplay.value }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.quality">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Quality</span></th>
            <td>{{ humanizeStringOrNumber(dialogData.dataPointDisplay.quality) }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.dataSource">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentDownloadLink
                :document-download-info="{
                  downloadName:
                    dialogData.dataPointDisplay.dataSource.fileName ??
                    dialogData.dataPointDisplay.dataSource.fileReference,
                  fileReference: dialogData.dataPointDisplay.dataSource.fileReference,
                  page: dataSourceFirstPageInRange,
                  dataId: dialogData.dataId,
                  dataType: dialogData.dataType,
                }"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataSourcePageRange">
            <th scope="row" class="headers-bg width-auto">
              <span class="table-left-label">{{ dataSourceHasMultiplePages ? 'Pages' : 'Page' }}</span>
            </th>
            <td>{{ dataSourcePageRange }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.comment">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Comment</span></th>
            <td>
              <RenderSanitizedMarkdownInput :text="dialogData.dataPointDisplay.comment" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants';
import { assertDefined } from '@/utils/TypeScriptUtils';
import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataPointDataTableRefProps } from '@/utils/Frameworks';
import { getPageInfo } from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';

export default defineComponent({
  methods: {
    humanizeStringOrNumber,
  },

  components: { RenderSanitizedMarkdownInput, DocumentDownloadLink },
  inject: ['dialogRef'],
  name: 'DataPointDataTable',

  data: () => {
    return { ONLY_AUXILIARY_DATA_PROVIDED };
  },

  computed: {
    dialogData(): DataPointDataTableRefProps {
      return assertDefined(this.dialogRef as DynamicDialogInstance).data as DataPointDataTableRefProps;
    },

    dataSourceFirstPageInRange(): number | undefined {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).firstPageInRange;
    },

    dataSourcePageRange(): string {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).pageRange;
    },

    dataSourceHasMultiplePages(): boolean {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).hasMultiplePages;
    },
  },
});
</script>

<style scoped lang="scss">
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}

.width-auto {
  width: auto;
}

.p-component {
  &:disabled {
    opacity: 0.8;
  }
}

.p-datatable {
  border-radius: 0;
  background: var(--table-background-color);
  color: var(--main-text-color);

  .border-left {
    border-left: 1px solid var(--table-border);
  }

  .border-right {
    border-right: 1px solid var(--table-border);
  }

  .border-bottom {
    border-bottom: 1px solid var(--table-border);
  }

  .horizontal-headers-size {
    background-color: var(--default-neutral-white);

    &:first-of-type {
      width: var(--first-table-column-width);
    }
  }
  .onlyHeaders {
    tr {
      display: flex;
      width: calc(100vw - 58px);
    }
    tr th:not(.first-horizontal-headers-size) {
      flex: 1;
    }
    .p-datatable-tbody {
      display: none;
    }
  }
  tr {
    &:not(.p-rowgroup-header) {
      td {
        border-bottom: 1px solid var(--table-border);
      }
    }
    &:hover {
      background: var(--table-background-hover-color);
    }
    th,
    td {
      text-align: left;
      padding: 1rem;
    }
  }
  .p-datatable-tbody {
    tr {
      border-color: hsl(from var(--table-border-dark) h s 45);
    }
    .info-icon {
      float: right;
      max-width: 20%;
    }
    .table-left-label {
      float: left;
      max-width: 80%;
    }
  }
  .p-sortable-column {
    .p-sortable-column-icon {
      color: var(--table-icon-color);
      margin-left: 0.5rem;
    }
    &.p-highlight {
      background: var(--table-background-color);
      color: var(--main-color);
      .p-sortable-column-icon {
        color: var(--main-color);
      }
    }
  }
  .headers-bg {
    background-color: var(--tables-headers-bg);
    display: table-cell;
    width: var(--first-table-column-width);
  }
  .auto-headers-size {
    width: auto;
  }
  .p-rowgroup-header {
    background-color: var(--table-background-hover-color-light);
    cursor: pointer;

    &.p-topmost-header {
      background-color: var(--tables-headers-bg);
    }

    td {
      position: relative;
      width: var(--first-table-column-width);
      button {
        position: absolute;
        right: 1rem;
        top: 50%;
        margin-top: -7px;
      }
    }
  }

  .p-datatable-thead {
    z-index: 1;
    tr {
      box-shadow: none;
      &:hover {
        background: var(--table-background-color);
      }
    }
  }

  &.activities-data-table {
    $col-activity-width: 300px;
    $col-nace-codes-width: 70px;
    .group-row-header {
      background-color: var(--tables-headers-bg);
      border-bottom: 1px solid var(--table-border);
      .p-column-header-content {
        justify-content: center;
      }
      &:not(:first-of-type) {
        border-left: 1px solid var(--table-border);
      }
    }
    .first-group-column:not(:first-of-type) {
      border-left: 1px solid var(--table-border);
    }
    .non-frozen-header {
      vertical-align: top;
    }
    .frozen-row-header {
      vertical-align: top;
      background-color: var(--tables-headers-bg);
    }
    .col-activity {
      width: $col-activity-width;
      min-width: $col-activity-width;
    }
    .col-nace-codes {
      width: $col-nace-codes-width;
      min-width: $col-nace-codes-width;
      border-right: 1px solid var(--table-border);
    }
    .col-value {
      width: 160px;
      min-width: 160px;
    }
    .col-percentage {
      min-width: 6rem;
    }
  }
}

.info-icon {
  cursor: help;
}

.p-component-overlay-enter {
  animation: p-component-overlay-enter-animation 150ms forwards;
}

.p-component-overlay-leave {
  animation: p-component-overlay-leave-animation 150ms forwards;
}
</style>
