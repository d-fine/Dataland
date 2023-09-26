import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";

/**
 * Returns a value factory that returns the value of the EU-Taxo Aligned Activities field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function euTaxonomyActivitiesDataTableValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const selectionValue = getFieldValueFromDataModel(path, dataset) as Array<string>;
    if (!selectionValue || selectionValue.length == 0) {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "",
      };
    } else {
      let modalComponent = undefined;
      if (field.component == "AlignedActivitiesDataTable") {
        modalComponent = AlignedActivitiesDataTable;
      } else if (field.component == "NonAlignedActivitiesDataTable") {
        modalComponent = NonAlignedActivitiesDataTable;
      } else {
        throw new Error(`The component ${field.component} is not a valid eu-taxo activities table`);
      }

      return <MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>>{
        displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
        displayValue: {
          label: `Show ${field.label}`,
          modalComponent: modalComponent,
          modalOptions: {
            props: {
              header: field.label,
              modal: true,
              dismissableMask: true,
            },
            data: {
              listOfRowContents: selectionValue,
              kpiKeyOfTable: field.name,
              columnHeaders: euTaxonomyForNonFinancialsModalColumnHeaders,
            },
          },
        },
      };
    }
  };
}
