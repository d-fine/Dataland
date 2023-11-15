import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

export type MLDTConfig<FrameworkDataType> = Array<
  MLDTCellConfig<FrameworkDataType> | MLDTSectionConfig<FrameworkDataType>
>;

export interface MLDTDataset<FrameworkDataType> {
  headerLabel: string;
  dataset: FrameworkDataType;
}

export type BadgeColor = "yellow" | "green" | "red" | "blue" | "purple" | "gray" | "brown" | "orange";

export interface MLDTCellConfig<FrameworkDataType> {
  type: "cell";
  label: string;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  valueGetter: (dataset: FrameworkDataType) => AvailableMLDTDisplayObjectTypes;
  explanation?: string;
}

export interface MLDTSectionConfig<FrameworkDataType> {
  type: "section";
  label: string;
  expandOnPageLoad: boolean;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  children: MLDTConfig<FrameworkDataType>;
  labelBadgeColor?: BadgeColor;
  areThisSectionAndAllParentSectionsDisplayedForTheDataset?: (dataset: FrameworkDataType) => boolean;
}

/**
 * Check if the cell or section should be displayed given the provided datasets.
 * @param cellOrSectionConfig The cellOrSectionConfig to check
 * @param mldtDatasets The datasets to use during the check
 * @returns true if the cell or section should be displayed
 */
export function isCellOrSectionVisible<FrameworkDataType>(
  cellOrSectionConfig: MLDTSectionConfig<FrameworkDataType> | MLDTCellConfig<FrameworkDataType>,
  mldtDatasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  if (cellOrSectionConfig.type == "cell") {
    return isCellRowVisible(cellOrSectionConfig, mldtDatasets);
  } else {
    return isCellSectionVisible(cellOrSectionConfig, mldtDatasets);
  }
}

/**
 * Check if the specified MLDT-Cell should be displayed given the provided datasets.
 * @param cellConfig The cell to check
 * @param mldtDatasets The datasets to use during the check
 * @returns true if the cell should be displayed
 */
function isCellRowVisible<FrameworkDataType>(
  cellConfig: MLDTCellConfig<FrameworkDataType>,
  mldtDatasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  return mldtDatasets.some((mldtDataset) => cellConfig.shouldDisplay(mldtDataset.dataset));
}

/**
 * Check if the specified MLDT-Section should be displayed given the provided datasets.
 * @param sectionConfig The section to check
 * @param mldtDatasets The datasets to use during the check
 * @returns true iff the section should be displayed
 */
function isCellSectionVisible<FrameworkDataType>(
  sectionConfig: MLDTSectionConfig<FrameworkDataType>,
  mldtDatasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  const shouldShowSection = mldtDatasets.some((mldtDataset) => sectionConfig.shouldDisplay(mldtDataset.dataset));

  if (!shouldShowSection) return false;

  const anyChildrenVisible = sectionConfig.children.some((child) => isCellOrSectionVisible(child, mldtDatasets));

  return shouldShowSection && anyChildrenVisible;
}
