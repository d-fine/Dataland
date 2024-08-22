import { type AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';

export type MLDTConfig<FrameworkDataType> = Array<
  MLDTCellConfig<FrameworkDataType> | MLDTSectionConfig<FrameworkDataType>
>;

export type BadgeColor = 'yellow' | 'green' | 'red' | 'blue' | 'purple' | 'gray' | 'brown' | 'orange';

export interface MLDTCellConfig<FrameworkDataType> {
  type: 'cell';
  label: string;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  valueGetter: (dataset: FrameworkDataType) => AvailableMLDTDisplayObjectTypes;
  explanation?: string;
  class?: string;
  name?: string;
}

export interface MLDTSectionConfig<FrameworkDataType> {
  type: 'section';
  label: string;
  expandOnPageLoad: boolean;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  children: MLDTConfig<FrameworkDataType>;
  labelBadgeColor?: BadgeColor;
  areThisSectionAndAllParentSectionsDisplayedForTheDataset?: (dataset: FrameworkDataType) => boolean;
  name?: string;
}

/**
 * Check if the cell or section should be displayed given the provided datasets.
 * @param cellOrSectionConfig The cellOrSectionConfig to check
 * @param dataAndMetaInformation The datasets to use during the check
 * @returns true if the cell or section should be displayed
 */
export function isCellOrSectionVisible<FrameworkDataType>(
  cellOrSectionConfig: MLDTSectionConfig<FrameworkDataType> | MLDTCellConfig<FrameworkDataType>,
  dataAndMetaInformation: Array<DataAndMetaInformation<FrameworkDataType>>
): boolean {
  if (cellOrSectionConfig.type == 'cell') {
    return isCellRowVisible(cellOrSectionConfig, dataAndMetaInformation);
  } else {
    return isCellSectionVisible(cellOrSectionConfig, dataAndMetaInformation);
  }
}

/**
 * Check if the specified MLDT-Cell should be displayed given the provided datasets.
 * @param cellConfig The cell to check
 * @param dataAndMetaInformation The datasets to use during the check
 * @returns true if the cell should be displayed
 */
function isCellRowVisible<FrameworkDataType>(
  cellConfig: MLDTCellConfig<FrameworkDataType>,
  dataAndMetaInformation: Array<DataAndMetaInformation<FrameworkDataType>>
): boolean {
  return dataAndMetaInformation.some((singleDataAndMetaInformation) =>
    cellConfig.shouldDisplay(singleDataAndMetaInformation.data)
  );
}

/**
 * Check if the specified MLDT-Section should be displayed given the provided datasets.
 * @param sectionConfig The section to check
 * @param dataAndMetaInformation The datasets to use during the check
 * @returns true iff the section should be displayed
 */
function isCellSectionVisible<FrameworkDataType>(
  sectionConfig: MLDTSectionConfig<FrameworkDataType>,
  dataAndMetaInformation: Array<DataAndMetaInformation<FrameworkDataType>>
): boolean {
  const shouldShowSection = dataAndMetaInformation.some((singleDataAndMetaInformation) =>
    sectionConfig.shouldDisplay(singleDataAndMetaInformation.data)
  );

  if (!shouldShowSection) {
    return false;
  }

  const anyChildrenVisible = sectionConfig.children.some((child) =>
    isCellOrSectionVisible(child, dataAndMetaInformation)
  );

  return shouldShowSection && anyChildrenVisible;
}
