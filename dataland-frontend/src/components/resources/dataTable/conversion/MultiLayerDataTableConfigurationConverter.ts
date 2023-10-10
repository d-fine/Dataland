import { type Category, type Subcategory } from "@/utils/GenericFrameworkTypes";
import {
  type BadgeColor,
  type MLDTConfig,
  type MLDTSectionConfig,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { getDataModelFieldDisplayConfiguration } from "@/components/resources/dataTable/conversion/MultiLayerDataTableFieldConverter";

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

const autoExpandingCategoryNames = new Set(["general", "masterData"]);

/**
 * Converts a Data-Model-Ts Category to a MLDTSectionConfig
 * @param category the category to convert
 * @returns the converted category
 */
function convertCategory(category: Category): MLDTSectionConfig<any> {
  const mldtCategoryChildren = category.subcategories.map((it) => convertSubCategory(category, it));

  return {
    type: "section",
    label: category.label,
    expandOnPageLoad: autoExpandingCategoryNames.has(category.name),
    children: mldtCategoryChildren,
    shouldDisplay: category.showIf,
    labelBadgeColor: category.color as BadgeColor,
  };
}

/**
 * Converts a Data-Model-Ts Subcategory to a MLDTSectionConfig
 * @param category the parent category
 * @param subcategory the subcategory to convert
 * @returns the converted subcategory
 */
function convertSubCategory(category: Category, subcategory: Subcategory): MLDTSectionConfig<any> {
  const mldtSubcategoryChildren: MLDTConfig<any> = [];

  for (const field of subcategory.fields) {
    const cellConfig = getDataModelFieldDisplayConfiguration(
      category.name + "." + subcategory.name + "." + field.name,
      field,
    );
    if (cellConfig) {
      mldtSubcategoryChildren.push(cellConfig);
    }
  }

  return {
    type: "section",
    label: subcategory.label,
    expandOnPageLoad: autoExpandingCategoryNames.has(subcategory.name),
    children: mldtSubcategoryChildren,
    shouldDisplay: () => true,
  };
}

/**
 * Converts a Data-Model-Ts DataModel to a Multi-Layer-Data-Table Display configuration
 * @param dataModel the data model to convert
 * @returns the view configuration
 */
export function convertDataModel(dataModel: Array<Category>): MLDTConfig<any> {
  return dataModel.map((category) => convertCategory(category));
}
