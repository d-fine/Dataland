import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  P2pDataControllerApi,
  SmeDataControllerApi,
  LksgDataControllerApi,
  SfdrDataControllerApi,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApi,
  type CompanyInformation,
} from "@clients/backend";
import { type AxiosPromise } from "axios";
import { type UploadIds } from "@e2e/utils/GeneralApiUtils";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";

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

const frameworkUploadConfigurations: {
  [Key in keyof FrameworkDataTypes]: FrameworkUploadConfiguration<
    FrameworkDataTypes[Key]["api"],
    FrameworkDataTypes[Key]["data"]
  >;
} = {
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
 * @param bypassQa Whether to bypass the QA process
 * @returns a promise on the created data meta information
 */
export async function uploadFrameworkData<K extends keyof FrameworkDataTypes>(
  framework: K,
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: FrameworkDataTypes[K]["data"],
  bypassQa = true,
): Promise<DataMetaInformation> {
  const frameworkConfig = frameworkUploadConfigurations[framework];
  const apiClient = new frameworkConfig.apiConstructor(new Configuration({ accessToken: token }));
  const response = await frameworkConfig.uploaderFactory(apiClient)(
    {
      companyId,
      reportingPeriod,
      data,
    },
    bypassQa,
  );
  return response.data;
}

/**
 * Uploads a company and single framework data entry for a company
 * @param framework The framework to upload data for
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @param reportingPeriod The reporting period to use for the upload
 * @param bypassQa Whether to bypass the QA process
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export async function uploadCompanyAndFrameworkData<K extends keyof FrameworkDataTypes>(
  framework: K,
  token: string,
  companyInformation: CompanyInformation,
  testData: FrameworkDataTypes[K]["data"],
  reportingPeriod: string,
  bypassQa = true,
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, companyInformation).then(async (storedCompany) => {
    return uploadFrameworkData(framework, token, storedCompany.companyId, reportingPeriod, testData, bypassQa).then(
      (dataMetaInformation) => {
        return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
      },
    );
  });
}
