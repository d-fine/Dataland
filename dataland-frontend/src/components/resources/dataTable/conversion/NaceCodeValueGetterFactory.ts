import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import MultiSelectModal from '@/components/resources/dataTable/modals/MultiSelectModal.vue';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import { convertSingleNaceCode } from '@/utils/NaceCodeConverter';

/**
 * Returns a value factory that returns the value of the NACE code form field.
 * If multiple values are selected, it returns a cell with a modal link. The modal displays all selected values.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function naceCodeValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const naceCodes = getFieldValueFromFrameworkDataset(path, dataset) as Array<string>;
    const modalLabel = field.label;
    return formatNaceCodesForDatatable(naceCodes, modalLabel);
  };
}

/**
 * Formats a list of nace codes for the datatable
 * @param naceCodes the nace codes to display
 * @param modalLabel the label that the modal which shows the nace codes shall have
 * @returns display object for the multi layer data table
 */
export function formatNaceCodesForDatatable(
  naceCodes: string[] | null | undefined,
  modalLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!naceCodes || naceCodes.length == 0) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show ${naceCodes.length} NACE code${naceCodes.length > 1 ? 's' : ''}`,
        modalComponent: MultiSelectModal,
        modalOptions: {
          props: {
            header: modalLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: modalLabel,
            values: naceCodes.map(convertSingleNaceCode),
          },
        },
      },
    };
  }
}
