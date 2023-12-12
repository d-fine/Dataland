import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type BaseDataPoint } from "@/utils/DataPoint";
import GdvListOfBaseDataPointModal from "@/components/resources/dataTable/modals/GdvListOfBaseDataPointModal.vue";

/**
 * Convert a list of string-wrapping base datapoints into a display object.
 * @param baseDataPoints to convert
 * @param fieldLabel of the field that holds this list of base datapoints
 * @param descriptionColumnHeader TODO
 * @param documentColumnHeader TODO
 * @returns a valid display object type for the MLDT
 */
export function formatListOfBaseDataPoint(
    fieldLabel: string,
    baseDataPoints: BaseDataPoint<string>[] | null | undefined,
    descriptionColumnHeader: string,
    documentColumnHeader: string,
): AvailableMLDTDisplayObjectTypes {
  if (!baseDataPoints) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show ${baseDataPoints.length} certificate${baseDataPoints.length > 1 ? "s" : ""}`,
        modalComponent: GdvListOfBaseDataPointModal,
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
