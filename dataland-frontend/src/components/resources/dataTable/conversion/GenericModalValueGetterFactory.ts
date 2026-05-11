import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import DetailsCompanyDataTable from '@/components/general/DetailsCompanyDataTable.vue';

type ColumnHeaderType = Record<string, Record<string, string>>;

/**
 * A factory that allows for the creation of generic modal value factories
 * @param kpiKeyOfTable the identifier of the table in the column headers
 * @param columnHeaders the human-readable column header map
 * @returns a factory for displaying the modal
 */
export function getModalGetterFactory(
  kpiKeyOfTable: string,
  columnHeaders: ColumnHeaderType
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (path: string, field: Field) => (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (path, field) => {
    return (dataset) => {
      return getDisplayValueForModal(
        kpiKeyOfTable,
        columnHeaders,
        field,
        getFieldValueFromFrameworkDataset(path, dataset) as Array<object>
      );
    };
  };
}

/**
 * Given the definition of a generic modal, generate a ModalLinkDisplayComponent that displays it
 * @param kpiKeyOfTable the identifier of the table in the column headers
 * @param columnHeaders the human-readable column header map
 * @param field the underlying field
 * @param fieldValue the value from the dataset
 * @returns a ModalLinkDisplayComponent to the modal (if any data is present).
 */
function getDisplayValueForModal(
  kpiKeyOfTable: string,
  columnHeaders: ColumnHeaderType,
  field: Field,
  fieldValue?: Array<object>
): AvailableMLDTDisplayObjectTypes {
  if (!fieldValue || fieldValue.length == 0) {
    return MLDTDisplayObjectForEmptyString;
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: `Show ${field.label}`,
      modalComponent: DetailsCompanyDataTable,
      modalOptions: {
        props: {
          header: field.label,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: fieldValue,
          kpiKeyOfTable: kpiKeyOfTable,
          columnHeaders: columnHeaders,
        },
      },
    },
  };
}
