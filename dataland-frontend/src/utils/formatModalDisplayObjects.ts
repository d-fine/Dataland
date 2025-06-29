import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type ExtendedDataPoint } from '@/utils/DataPoint.ts';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory.ts';
import { type Component } from 'vue';

/**
 * Helper to create a modal display object for MLDT
 */
export function createModalDisplayObject<T>(
  data: ExtendedDataPoint<T[]> | null | undefined,
  fieldLabel: string,
  modalComponent: Component,
  tableKey: string,
  columnHeaders: Record<string, Record<string, string>>,
  kpiType?: 'revenue' | 'capex' | 'opex'
): AvailableMLDTDisplayObjectTypes {
  if (!data) {
    return MLDTDisplayObjectForEmptyString;
  }

  const labelSuffix = (data.value?.length ?? 0) > 1 ? 'ies' : 'y';

  return {
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${data.value?.length} activit${labelSuffix}`,
      modalComponent,
      modalOptions: {
        props: {
          header: kpiType
            ? `${fieldLabel} (${kpiType === 'capex' ? 'CapEx' : kpiType === 'opex' ? 'OpEx' : 'Revenue'})`
            : fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: data.value,
          kpiKeyOfTable: tableKey,
          columnHeaders,
          dataPointDisplay: {
            dataSource: data.dataSource,
            comment: data.comment,
            quality: data.quality,
          },
          ...(kpiType ? { kpiType } : {}),
        },
      },
    },
  };
}

/**
 * Returns aligned activities headers dynamically adjusted for the KPI type.
 */
export function getDynamicAlignedColumnHeaders(
  kpiType: 'revenue' | 'capex' | 'opex'
): typeof euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities {
  const typeLabels = {
    revenue: 'Revenue',
    capex: 'CapEx',
    opex: 'OpEx',
  };

  return {
    ...euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities,
    revenue: typeLabels[kpiType],
    revenuePercent: `${typeLabels[kpiType]} (%)`,
  };
}
