import { type DocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: DocumentReference };
  [MLDTDisplayComponents.ModalLinkDisplayComponent]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
};

export type AvailableDisplayValues =
  | MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>
  | MLDTDisplayValue<MLDTDisplayComponents.DocumentLinkDisplayComponent>
  | MLDTDisplayValue<MLDTDisplayComponents.ModalLinkDisplayComponent>;

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
