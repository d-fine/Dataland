import {
  type DataTypeEnum,
  type P2pDataControllerApi,
  type PathwaysToParisData,
} from '@clients/backend';

export type FrameworkDataTypes = {
  [DataTypeEnum.P2p]: {
    data: PathwaysToParisData;
    apiSuffix: 'P2pData';
    api: P2pDataControllerApi;
  };
};
