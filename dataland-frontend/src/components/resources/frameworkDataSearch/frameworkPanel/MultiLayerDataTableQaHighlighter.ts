import {
  type MLDTCellConfig,
  type MLDTConfig,
  type MLDTSectionConfig,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

/**
 * For QA it is desirable that all fields are displayed even if they should not normally be visible. This function
 * converts a standard view-configuration to one that displays all cells but highlights cells that would
 * not normally be displayed
 * @param config the input view configuration
 * @returns the modified hidden-highlighting view-config
 */
export function convertMultiLayerDataTableConfigForHighlightingHiddenFields<T>(config: MLDTConfig<T>): MLDTConfig<T> {
  return config.map((it) => {
    if (it.type == "cell") return wrapMultiLayerDataTableCellForHighlightingHiddenFields(it);
    else return wrapMultiLayerDataTableSectionForHighlightingHiddenFields(it);
  });
}

/**
 * Converts a single section (and all it's children) to the show-always directive
 * @param sectionConfig the section config to convert
 * @returns the modified section config
 */
function wrapMultiLayerDataTableSectionForHighlightingHiddenFields<T>(
  sectionConfig: MLDTSectionConfig<T>,
): MLDTSectionConfig<T> {
  return {
    ...sectionConfig,
    shouldDisplay: () => true,
    children: convertMultiLayerDataTableConfigForHighlightingHiddenFields(sectionConfig.children),
  };
}

/**
 * Converts a single cell to the show-always directive
 * @param cellConfig the section config to convert
 * @returns the modified cell config
 */
function wrapMultiLayerDataTableCellForHighlightingHiddenFields<T>(cellConfig: MLDTCellConfig<T>): MLDTCellConfig<T> {
  return {
    ...cellConfig,
    shouldDisplay: () => true,
    valueGetter: (dataset: T): AvailableMLDTDisplayObjectTypes => {
      const originalDisplayValue = cellConfig.valueGetter(dataset);

      if (cellConfig.shouldDisplay(dataset)) {
        return originalDisplayValue;
      } else {
        return {
          displayComponentName: MLDTDisplayComponentName.HighlightHiddenCellDisplayComponent,
          displayValue: {
            innerContents: originalDisplayValue,
          },
        };
      }
    },
  };
}
