import { type BaseDocumentReference, type ExtendedDocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponentName {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
  HighlightHiddenCellDisplay = "HighlightHiddenCellDisplay",
  DataPointDisplayComponent = "DataPointDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponentName.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponentName.DocumentLinkDisplayComponent]: { label: string; dataSource: BaseDocumentReference };
  [MLDTDisplayComponentName.DataPointDisplayComponent]: {
    fieldLabel: string;
    value: string;
    dataSource: ExtendedDocumentReference | BaseDocumentReference;
    quality?: string;
    comment?: string;
  };
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
