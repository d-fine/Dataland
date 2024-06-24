import {
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForFinancialsControllerApi,
  type P2pDataControllerApi,
  type PathwaysToParisData,
} from '@clients/backend';

/**
 * Check if a framework is a legacy framework
 * @param frameworkIdentifier The identifier of the framework
 * @returns True if the framework is a legacy framework
 */
export function isLegacyFramework(frameworkIdentifier: string): frameworkIdentifier is keyof FrameworkDataTypes {
  return frameworkIdentifier in legacyFrameworks;
}

export const legacyFrameworks: { [key in keyof FrameworkDataTypes]: true } = {
  [DataTypeEnum.P2p]: true,
  [DataTypeEnum.EutaxonomyFinancials]: true,
};

export type FrameworkDataTypes = {
  [DataTypeEnum.P2p]: {
    data: PathwaysToParisData;
    apiSuffix: 'P2pData';
    api: P2pDataControllerApi;
  };
  [DataTypeEnum.EutaxonomyFinancials]: {
    data: EuTaxonomyDataForFinancials;
    apiSuffix: 'EuTaxonomyDataForFinancials';
    api: EuTaxonomyDataForFinancialsControllerApi;
  };
};
