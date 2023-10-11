import { type BaseDocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponentName {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
  HighlightHiddenCellDisplayComponent = "HighlightHiddenCellDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponentName.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponentName.DocumentLinkDisplayComponent]: { label: string; reference: BaseDocumentReference };
  [MLDTDisplayComponentName.ModalLinkDisplayComponent]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
  [MLDTDisplayComponentName.HighlightHiddenCellDisplayComponent]: {
    innerContents: AvailableDisplayValues;
  };
};

export type AvailableDisplayValues = {
  [K in MLDTDisplayComponentName]: MLDTDisplayValue<K>;
}[MLDTDisplayComponentName];

export const EmptyDisplayValue: AvailableDisplayValues = {
  displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
  displayValue: "",
};

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponentName> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
