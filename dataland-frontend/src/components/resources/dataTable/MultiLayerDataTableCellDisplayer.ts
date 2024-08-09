import { type BaseDocumentReference, type ExtendedDocumentReference } from '@clients/backend';
import { type DynamicDialogOptions } from 'primevue/dynamicdialogoptions';
import { type DataPointDisplay } from '@/utils/DataPoint';

export enum MLDTDisplayComponentName {
  StringDisplayComponent = 'StringDisplayComponent',
  FreeTextDisplayComponent = 'FreeTextDisplayComponent',
  DocumentLinkDisplayComponent = 'DocumentLinkDisplayComponent',
  ModalLinkDisplayComponent = 'ModalLinkDisplayComponent',
  HighlightHiddenCellDisplay = 'HighlightHiddenCellDisplay',
  DataPointDisplayComponent = 'DataPointDisplayComponent',
  DataPointWrapperDisplayComponent = 'DataPointWrapperDisplayComponent',
  ModalLinkWithDataSourceDisplayComponent = 'ModalLinkWithDataSourceDisplayComponent',
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponentName.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponentName.FreeTextDisplayComponent]: string;
  [MLDTDisplayComponentName.DocumentLinkDisplayComponent]: { label: string; dataSource: BaseDocumentReference };
  [MLDTDisplayComponentName.DataPointDisplayComponent]: {
    fieldLabel: string;
  } & DataPointDisplay;
  [MLDTDisplayComponentName.ModalLinkDisplayComponent]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
  [MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
  [MLDTDisplayComponentName.HighlightHiddenCellDisplay]: {
    innerContents: AvailableMLDTDisplayObjectTypes;
  };
  [MLDTDisplayComponentName.DataPointWrapperDisplayComponent]: {
    innerContents: AvailableMLDTDisplayObjectTypes;
    fieldLabel: string;
    quality?: string;
    dataSource?: ExtendedDocumentReference | BaseDocumentReference | null;
    comment?: string;
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
  displayValue: '',
};
