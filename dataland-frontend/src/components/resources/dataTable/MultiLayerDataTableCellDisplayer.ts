import { type BaseDocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponentName {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
  HighlightHiddenCellDisplay = "HighlightHiddenCellDisplay",
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
  [MLDTDisplayComponentName.HighlightHiddenCellDisplay]: {
    innerContents: AvailableMLDTDisplayObjectTypes;
  };
};

export interface MLDTDisplayObject<DisplayComponentName extends MLDTDisplayComponentName> {
  displayComponentName: DisplayComponentName;
  displayValue: MLDTDisplayComponentTypes[DisplayComponentName];
}

export type AvailableMLDTDisplayObjectTypes = {
  [DisplayComponentName in MLDTDisplayComponentName]: MLDTDisplayObject<DisplayComponentName>;
}[MLDTDisplayComponentName];

export const MLDTDisplayObjectForEmptyString: AvailableMLDTDisplayObjectTypes = {
  displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
  displayValue: "",
};
