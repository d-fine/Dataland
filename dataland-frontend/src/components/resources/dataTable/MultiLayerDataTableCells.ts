import { type DocumentReference } from "@clients/backend";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: DocumentReference };
};

export type AvailableDisplayValues =
  | MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>
  | MLDTDisplayValue<MLDTDisplayComponents.DocumentLinkDisplayComponent>;

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
