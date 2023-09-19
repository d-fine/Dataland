import { type Category, type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  type MLDTCellConfig,
  type MLDTConfig,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { formatNumberToReadableFormat, formatPercentageNumberAsString } from "@/utils/Formatter";
import { type BaseDataPointYesNo } from "@clients/backend";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";

function getFieldValueFromDataModel(identifier: string, dataModel: any): any {
  const splits = identifier.split(".");
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject == undefined || currentObject == null) return currentObject;
    currentObject = currentObject[split];
  }
  return currentObject;
}

function getValueGetterForYesNoField(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    if (field?.certificateRequiredIfYes == true) {
      const elementValue = getFieldValueFromDataModel(path, dataset) as BaseDataPointYesNo;
      if (elementValue.dataSource) {
        const dataSource = elementValue.dataSource;
        return {
          displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
          displayValue: {
            label: elementValue.value + " (Certified)",
            reference: dataSource,
          },
        };
      }
    }

    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: getFieldValueFromDataModel(path, dataset) as string,
    };
  };
}
function getValueGetterForMultiSelectField(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  const nameMap = new Map<string, string>();
  for (const option of field.options || []) {
    nameMap.set(option.value, option.label);
  }

  return (dataset) => {
    const selectionValue = getFieldValueFromDataModel(path, dataset) as Array<string>;
    if (!selectionValue || selectionValue.length == 0) {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "",
      };
    } else if (selectionValue.length == 1) {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: nameMap.get(selectionValue[0]),
      };
    } else {
      return {
        displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
        displayValue: {
          label: `Show ${selectionValue.length} values`,
          modalComponent: MultiSelectModal,
          modalOptions: {
            props: {
              header: field.label,
              modal: true,
              dismissableMask: true,
            },
            data: {
              label: field.label,
              values: selectionValue.map((it) => nameMap.get(it)),
            },
          },
        },
      };
    }
  };
}

function getDataModelFieldDisplayConfiguration(path: string, field: Field): MLDTCellConfig<any> | undefined {
  const baseConfiguration = {
    type: "cell",
    label: field.label,
    explanation: field.description,
    shouldDisplay: field.showIf,
  };
  switch (field.component) {
    case "YesNoFormField":
      return {
        ...baseConfiguration,
        valueGetter: getValueGetterForYesNoField(path, field),
      };
    case "MultiSelectFormField":
      return {
        ...baseConfiguration,
        valueGetter: getValueGetterForMultiSelectField(path, field),
      };
    case "AddressFormField":
    case "DateFormField":
    case "InputTextFormField":
      return {
        ...baseConfiguration,
        valueGetter: (dataset) => ({
          displayComponent: MLDTDisplayComponents.StringDisplayComponent,
          displayValue: getFieldValueFromDataModel(path, dataset) as string,
        }),
      };
    case "NumberFormField":
      return {
        ...baseConfiguration,
        valueGetter: (dataset) => ({
          displayComponent: MLDTDisplayComponents.StringDisplayComponent,
          displayValue: formatNumberToReadableFormat(getFieldValueFromDataModel(path, dataset) as number),
        }),
      };
    case "PercentageFormField":
      return {
        ...baseConfiguration,
        valueGetter: (dataset) => ({
          displayComponent: MLDTDisplayComponents.StringDisplayComponent,
          displayValue: formatPercentageNumberAsString(getFieldValueFromDataModel(path, dataset) as number),
        }),
      };
  }
  console.log("UNDEFINED: " + field.component);
  return undefined;
}

export function convertDataModel(dataModel: Array<Category>): MLDTConfig<any> {
  const mldtConfig: MLDTConfig<any> = [];

  for (const category of dataModel) {
    const mldtCategoryChildren: MLDTConfig<any> = [];

    for (const subcategory of category.subcategories) {
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

      mldtCategoryChildren.push({
        type: "section",
        label: subcategory.label,
        expandOnPageLoad: false,
        children: mldtSubcategoryChildren,
        shouldDisplay: (dataset) => true,
      });
    }
    mldtConfig.push({
      type: "section",
      label: category.label,
      expandOnPageLoad: false,
      children: mldtCategoryChildren,
      shouldDisplay: category.showIf,
      labelBadgeColor: category.color,
    });
  }

  return mldtConfig;
}
