import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type Component } from 'vue';
import { type ExtendedDataPoint } from '@/utils/DataPoint.ts';

type CreateModalDisplayObject<T> = {
  activities: ExtendedDataPoint<T[]>;
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
 *  @param fieldLabel - label of the field as shown in the modal title (e.g., "Non-Aligned Activities").
 *  @param kpiType - KPI category this data belongs to; affects labels and column headers ('revenue' | 'capex' | 'opex').
 *  @param tableKey - key identifying the specific column header set in the `columnHeaders` map.
 *  @param columnHeaders - map of column header configurations indexed by KPI-specific keys.
 *  @param modalComponent -  Vue component used to display the list inside the modal.
 */
export function createModalDisplayObject<T>({
  activities,
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
  const activityList = activities.value ?? [];

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${activityList.length} activit${activityList.length !== 1 ? 'ies' : 'y'}`,
      modalComponent,
      modalOptions: {
        props: {
          header: `${fieldLabel} (${typeLabels[kpiType]})`,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: activityList,
          kpiKeyOfTable: tableKey,
          columnHeaders,
          dataPointDisplay: {
            dataSource: activities.dataSource,
            comment: activities.comment,
            quality: activities.quality,
          },
          kpiType,
        },
      },
    },
  };
}
