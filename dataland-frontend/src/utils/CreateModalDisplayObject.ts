import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type ExtendedDocumentReference, type QualityOptions } from '@clients/backend';
import { type Component } from 'vue';

type CreateModalDisplayObject<T> = {
  activities: T[];
  dataSource?: ExtendedDocumentReference | null | undefined;
  comment?: string | null | undefined
  quality?: QualityOptions | null | undefined;
  fieldLabel: string;
  kpiType: 'revenue' | 'capex' | 'opex';
  tableKey: string;
  columnHeaders: Record<string, unknown>;
  modalComponent: Component;
};

/**
 * Helper function to create a modal display object for MLDT
 *  @param activities - list of activity objects to be displayed in the modal.
 *  @param dataSource - source metadata for the data point (e.g., 'Reported by company').
 *  @param comment -  user or system comment explaining the data point.
 *  @param quality -  data quality indicator.
 *  @param fieldLabel - label of the field as shown in the modal title (e.g., "Non-Aligned Activities").
 *  @param kpiType - KPI category this data belongs to; affects labels and column headers ('revenue' | 'capex' | 'opex').
 *  @param tableKey - key identifying the specific column header set in the `columnHeaders` map.
 *  @param columnHeaders - map of column header configurations indexed by KPI-specific keys.
 *  @param modalComponent -  Vue component used to display the list inside the modal.
 */
export function createModalDisplayObject<T>({
  activities,
  dataSource,
  comment,
  quality,
  fieldLabel,
  kpiType,
  tableKey,
  columnHeaders,
  modalComponent,
}: CreateModalDisplayObject<T>): AvailableMLDTDisplayObjectTypes {
  const typeLabels = {
    revenue: 'Revenue',
    capex: 'CapEx',
    opex: 'OpEx',
  };

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${activities.length} activit${activities.length > 1 ? 'ies' : 'y'}`,
      modalComponent,
      modalOptions: {
        props: {
          header: `${fieldLabel} (${typeLabels[kpiType]})`,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: activities,
          kpiKeyOfTable: tableKey,
          columnHeaders,
          dataPointDisplay: {
            dataSource,
            comment,
            quality,
          },
          kpiType,
        },
      },
    },
  };
}
