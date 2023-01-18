import {DataMetaInformation} from "@clients/backend";

export function convertListOfDataMetaInfoToListOfDataIds(receivedListOfDataMetaInfo: []) {
  return receivedListOfDataMetaInfo.map((dataMetaInfo) => (dataMetaInfo as DataMetaInformation).dataId);
}
