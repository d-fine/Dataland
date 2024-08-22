import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import { type Field } from '@/utils/GenericFrameworkTypes';
import { type P2pDriveMix } from '@clients/backend';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import DetailsCompanyDataTable from '@/components/general/DetailsCompanyDataTable.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DriveMixType } from '@/api-models/DriveMixType';
import { p2pModalColumnHeaders } from '@/components/resources/frameworkDataSearch/p2p/P2pModalColumnHeaders';

export type P2pDriveMixPerFleetType = { [key in DriveMixType]?: P2pDriveMix };

interface P2pDriveMixDisplayFormat {
  driveMixType: string;
  driveMixPerFleetSegmentInPercent: string;
  totalAmountOfVehicles: string;
}

/**
 * Convert an object of type Map<DriveMixType, P2pDriveMix> into a list that can be displayed using the standard
 * modal DataTable
 * @param driveMixPerFleetSegment the value for the field driveMixPerFleetSegment
 * @returns the converted list
 */
function convertP2pDriveMixTypeToListForModal(
  driveMixPerFleetSegment: P2pDriveMixPerFleetType
): P2pDriveMixDisplayFormat[] {
  const listForModal: P2pDriveMixDisplayFormat[] = [];
  for (const [driveMixType, p2pDriveMix] of Object.entries(driveMixPerFleetSegment)) {
    listForModal.push({
      driveMixType: humanizeStringOrNumber(driveMixType),
      driveMixPerFleetSegmentInPercent:
        p2pDriveMix.driveMixPerFleetSegmentInPercent != null
          ? formatPercentageNumberAsString(p2pDriveMix.driveMixPerFleetSegmentInPercent)
          : '',
      totalAmountOfVehicles:
        p2pDriveMix.totalAmountOfVehicles != null ? p2pDriveMix.totalAmountOfVehicles.toString() : '',
    });
  }
  return listForModal;
}

/**
 * Returns a value factory that returns the value of the drive-mix-per-fleet-segment field as a modal
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the underlying form field
 * @returns the created getter
 */
export function p2pDriveMixValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const driveMixPerFleetSegment = getFieldValueFromFrameworkDataset(path, dataset) as P2pDriveMixPerFleetType;
    if (!driveMixPerFleetSegment || Object.keys(driveMixPerFleetSegment).length == 0) {
      return MLDTDisplayObjectForEmptyString;
    }

    const convertedDriveMixPerFleetSegmentForModal = convertP2pDriveMixTypeToListForModal(driveMixPerFleetSegment);

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
            listOfRowContents: convertedDriveMixPerFleetSegmentForModal,
            kpiKeyOfTable: 'driveMixPerFleetSegment',
            columnHeaders: p2pModalColumnHeaders,
          },
        },
      },
    };
  };
}
