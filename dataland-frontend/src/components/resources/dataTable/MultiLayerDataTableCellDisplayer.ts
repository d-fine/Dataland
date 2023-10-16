import { type BaseDocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponentName {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponentName = "DocumentLinkDisplayComponentName",
  ModalLinkDisplayComponentName = "ModalLinkDisplayComponentName",
  HighlightHiddenCellDisplayComponentName = "HighlightHiddenCellDisplayComponentName",
  DataPointDisplayComponentName = "DataPointDisplayComponentName",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponentName.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponentName.DocumentLinkDisplayComponentName]: { label: string; dataSource: BaseDocumentReference };
  [MLDTDisplayComponentName.DataPointDisplayComponentName]: {
    label: string;
    fileName: string;
    fileReference: string;
    page?: number;
  };
  [MLDTDisplayComponentName.ModalLinkDisplayComponentName]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
  [MLDTDisplayComponentName.HighlightHiddenCellDisplayComponentName]: {
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
