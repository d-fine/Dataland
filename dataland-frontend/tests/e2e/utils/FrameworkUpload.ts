import {
  Configuration,
  DataMetaInformation,
  NewEuTaxonomyDataForNonFinancialsControllerApi,
  NewEuTaxonomyDataForNonFinancials,
  DataTypeEnum,
  PathwaysToParisData,
  P2pDataControllerApi,
  SmeData,
  SmeDataControllerApi,
  LksgData,
  LksgDataControllerApi,
  SfdrData,
  SfdrDataControllerApi,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
} from "@clients/backend";
import { AxiosPromise } from "axios";

interface CompanyAssociatedFrameworkData<FrameworkDataType> {
  companyId: string;
  reportingPeriod: string;
  data: FrameworkDataType;
}

type FrameworkDataUploadFunction<FrameworkDataType> = (
  companyAssociatedData: CompanyAssociatedFrameworkData<FrameworkDataType>,
  bypassQa?: boolean,
) => AxiosPromise<DataMetaInformation>;

interface FrameworkUploadConfiguration<ApiClientType, FrameworkDataType> {
  apiConstructor: new (configuration: Configuration | undefined) => ApiClientType;
  uploaderFactory: (client: ApiClientType) => FrameworkDataUploadFunction<FrameworkDataType>;
}

export type FrameworkDataTypes = {
  [DataTypeEnum.NewEutaxonomyNonFinancials]: {
    data: NewEuTaxonomyDataForNonFinancials;
    api: NewEuTaxonomyDataForNonFinancialsControllerApi;
  };
  [DataTypeEnum.P2p]: {
    data: PathwaysToParisData;
    api: P2pDataControllerApi;
  };
  [DataTypeEnum.Sme]: {
    data: SmeData;
    api: SmeDataControllerApi;
  };
  [DataTypeEnum.Lksg]: {
    data: LksgData;
    api: LksgDataControllerApi;
  };
  [DataTypeEnum.Sfdr]: {
    data: SfdrData;
    api: SfdrDataControllerApi;
  };
  [DataTypeEnum.EutaxonomyFinancials]: {
    data: EuTaxonomyDataForFinancials;
    api: EuTaxonomyDataForFinancialsControllerApi;
  };
  [DataTypeEnum.EutaxonomyNonFinancials]: {
    data: EuTaxonomyDataForNonFinancials;
    api: EuTaxonomyDataForNonFinancialsControllerApi;
  };
};

const frameworkUploadConfigurations: {
  [Key in keyof FrameworkDataTypes]: FrameworkUploadConfiguration<
    FrameworkDataTypes[Key]["api"],
    FrameworkDataTypes[Key]["data"]
  >;
} = {
  [DataTypeEnum.NewEutaxonomyNonFinancials]: {
    apiConstructor: NewEuTaxonomyDataForNonFinancialsControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedNewEuTaxonomyDataForNonFinancials.bind(client),
  },
  [DataTypeEnum.P2p]: {
    apiConstructor: P2pDataControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedP2pData.bind(client),
  },
  [DataTypeEnum.Sme]: {
    apiConstructor: SmeDataControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedSmeData.bind(client),
  },
  [DataTypeEnum.Lksg]: {
    apiConstructor: LksgDataControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedLksgData.bind(client),
  },
  [DataTypeEnum.Sfdr]: {
    apiConstructor: SfdrDataControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedSfdrData.bind(client),
  },
  [DataTypeEnum.EutaxonomyFinancials]: {
    apiConstructor: EuTaxonomyDataForFinancialsControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedEuTaxonomyDataForFinancials.bind(client),
  },
  [DataTypeEnum.EutaxonomyNonFinancials]: {
    apiConstructor: EuTaxonomyDataForNonFinancialsControllerApi,
    uploaderFactory: (client) => client.postCompanyAssociatedEuTaxonomyDataForNonFinancials.bind(client),
  },
};

/**
 * Uploads a single framework entry for a company
 * @param framework The framework to upload data for
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadFrameworkData<K extends keyof FrameworkDataTypes>(
  framework: K,
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: FrameworkDataTypes[K]["data"],
): Promise<DataMetaInformation> {
  const frameworkConfig = frameworkUploadConfigurations[framework];
  const apiClient = new frameworkConfig.apiConstructor(new Configuration({ accessToken: token }));
  const response = await frameworkConfig.uploaderFactory(apiClient)(
    {
      companyId,
      reportingPeriod,
      data,
    },
    true,
  );
  return response.data;
}
