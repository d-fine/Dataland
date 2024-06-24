import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import ListOfBaseDataPointModal from '@/components/resources/dataTable/modals/ListOfBaseDataPointModal.vue';
import { type BaseDataPointString } from '@clients/backend';

/**
 * Convert a list of string-wrapping base datapoints into a display object.
 * @param fieldLabel of the field that holds this list of base datapoints
 * @param baseDataPoints to convert
 * @param descriptionColumnHeader will appear as column header on the description column
 * @param documentColumnHeader will appear as column header on the documents column
 * @returns a valid display object type for the MLDT
 */
export function formatListOfBaseDataPoint(
  fieldLabel: string,
  baseDataPoints: BaseDataPointString[] | null | undefined,
  descriptionColumnHeader: string,
  documentColumnHeader: string
): AvailableMLDTDisplayObjectTypes {
  if (!baseDataPoints || baseDataPoints.length === 0) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show ${baseDataPoints.length} value${baseDataPoints.length > 1 ? 's' : ''}`,
        modalComponent: ListOfBaseDataPointModal,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: fieldLabel,
            input: baseDataPoints,
            descriptionColumnHeader: descriptionColumnHeader,
            documentColumnHeader: documentColumnHeader,
          },
        },
      },
    };
  }
}
