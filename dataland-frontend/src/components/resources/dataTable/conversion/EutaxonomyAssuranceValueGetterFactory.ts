import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type AssuranceDataPoint, type AssuranceDataPointValueEnum } from '@clients/backend';
import { wrapDisplayValueWithDatapointInformation } from '@/components/resources/dataTable/conversion/DataPoints';

const nameMapping: Record<AssuranceDataPointValueEnum, string> = {
  None: 'None',
  LimitedAssurance: 'Limited Assurance',
  ReasonableAssurance: 'Reasonable Assurance',
};

/**
 * Formats the provided assurance datapoint for the datatable
 * @param assurance the assurance object to display
 * @param fieldLabel the label of the assurance datapoint
 * @returns the value formatted for display
 */
export function formatAssuranceForDataTable(
  assurance: AssuranceDataPoint | undefined | null,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!assurance) {
    return MLDTDisplayObjectForEmptyString;
  }

  const assuranceDatapointText = assurance.value != null ? nameMapping[assurance.value] : '';
  const innerDisplayValue: AvailableMLDTDisplayObjectTypes = {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: assuranceDatapointText,
  };

  return wrapDisplayValueWithDatapointInformation(innerDisplayValue, fieldLabel, {
    dataSource: assurance.dataSource,
    comment: assurance.provider ? `Assurance provided by ${assurance.provider}` : undefined,
  });
}

/**
 * Extracts the assurance provider from the provided assurance datapoint
 * @param assurance the assurance object to display
 * @returns the value formatted for display
 */
export function formatAssuranceProviderForDataTable(
  assurance: AssuranceDataPoint | undefined | null
): AvailableMLDTDisplayObjectTypes {
  if (!assurance?.provider) {
    return MLDTDisplayObjectForEmptyString;
  }

  return {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: assurance.provider,
  };
}
