import { DataMetaInformation } from "@clients/backend";

export function convertListOfDataMetaInfoToListOfDataIds(receivedListOfDataMetaInfo: []): string[] {
  return receivedListOfDataMetaInfo.map((dataMetaInfo) => (dataMetaInfo as DataMetaInformation).dataId);
}
