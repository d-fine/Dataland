import { type BaseDataPoint, type ExtendedDataPoint } from '@/utils/DataPoint';
import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import { type BaseDocumentReference, type ExtendedDocumentReference } from '@clients/backend';
import { NO_DATA_PROVIDED, ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants';
import { formatStringForDatatable } from '@/components/resources/dataTable/conversion/PlainStringValueGetterFactory';
/**
 * Checks if a given data point has a valid reference set
 * @param dataPoint the datapoint whose reference to check
 * @param dataPoint.dataSource the data source of the data point
 * @returns true if the reference is properly set
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function hasDataPointValidReference(dataPoint: ExtendedDataPoint<any> | BaseDataPoint<any>): boolean {
  return !!dataPoint?.dataSource?.fileReference?.trim().length;
}

/**
 * Returns the document references globally available in a dataset
 * @param path the path to the field value
 * @param field the field
 * @param formatter a function to transform the datapoint value ot a display value
 * @returns the data point getter factory
 */
export function getDataPointGetterFactory<
  V,
  D extends BaseDataPoint<V | null | undefined> | ExtendedDataPoint<V> = ExtendedDataPoint<V>,
>(
  path: string,
  field: Field,
  formatter: (dataPoint?: D) => string | undefined
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const dataPoint = getFieldValueFromFrameworkDataset(path, dataset) as D | undefined;
    if (!dataPoint) {
      return MLDTDisplayObjectForEmptyString;
    }
    const formattedValue = formatter(dataPoint);
    let displayValue: string;
    if (formattedValue == undefined || formattedValue == '') {
      displayValue = NO_DATA_PROVIDED;
    } else {
      displayValue = formattedValue;
    }
    const dataPointAsExtendedDataPoint = dataPoint as unknown as ExtendedDataPoint<V>;
    if (
      dataPointAsExtendedDataPoint.quality != null ||
      dataPointAsExtendedDataPoint.comment?.length ||
      dataPointAsExtendedDataPoint.dataSource?.page != null
    ) {
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
        displayValue: {
          fieldLabel: field.label,
          value: displayValue,
          dataSource: dataPointAsExtendedDataPoint.dataSource,
          quality: dataPointAsExtendedDataPoint.quality,
          comment: dataPointAsExtendedDataPoint.comment,
        },
      } as AvailableMLDTDisplayObjectTypes;
    } else if (hasDataPointValidReference(dataPoint)) {
      return {
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: displayValue,
          dataSource: dataPoint.dataSource,
        },
      } as AvailableMLDTDisplayObjectTypes;
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: displayValue,
      } as AvailableMLDTDisplayObjectTypes;
    }
  };
}

interface DatapointProperties {
  quality?: string | null;
  dataSource?: ExtendedDocumentReference | BaseDocumentReference | null;
  comment?: string | null;
}

/**
 * Wraps an existing MLDTDisplayValue with a datapoint document-support display value
 * @param inputValue the original value to wrap
 * @param fieldLabel the label of the field to wrap
 * @param datapointProperties the properties of the datapoint to wrap
 * @returns the wrapped display-value
 */
export function wrapDisplayValueWithDatapointInformation(
  inputValue: AvailableMLDTDisplayObjectTypes,
  fieldLabel: string,
  datapointProperties: DatapointProperties | undefined | null
): AvailableMLDTDisplayObjectTypes {
  if (inputValue === undefined) {
    return MLDTDisplayObjectForEmptyString;
  }
  if (doesAnyDataPointPropertyExist(datapointProperties)) {
    return {
      displayComponentName: MLDTDisplayComponentName.DataPointWrapperDisplayComponent,
      displayValue: {
        innerContents:
          inputValue.displayValue == '' ? formatStringForDatatable(ONLY_AUXILIARY_DATA_PROVIDED) : inputValue,
        quality: datapointProperties?.quality ?? undefined,
        comment: datapointProperties?.comment ?? undefined,
        dataSource: datapointProperties?.dataSource ?? undefined,
        fieldLabel: fieldLabel,
      },
    };
  } else if (inputValue.displayValue == '') {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return {
      displayComponentName: MLDTDisplayComponentName.DataPointWrapperDisplayComponent,
      displayValue: {
        innerContents: inputValue,
      },
    } as AvailableMLDTDisplayObjectTypes;
  }
}

/**
 * Checks if any property of the data point is not null.
 * Has to check for Quality != NA, since this is the default setting for no provided data.
 * @param dataPointProperties gives dataPoint properties
 * @returns boolean value
 */
function doesAnyDataPointPropertyExist(dataPointProperties: DatapointProperties | null | undefined): boolean {
  return <boolean>(
    (dataPointProperties?.quality != null ||
      dataPointProperties?.comment?.length ||
      (dataPointProperties?.dataSource && dataPointProperties?.dataSource.fileReference.trim().length > 0))
  );
}
