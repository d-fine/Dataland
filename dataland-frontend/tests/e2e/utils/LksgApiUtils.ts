import {
  Configuration,
  LksgData,
  LksgDataControllerApi,
  DataMetaInformation,
  CompanyInformation
} from "@clients/backend";
import {UploadIds} from "./GeneralApiUtils";
import {getKeycloakToken} from "./Auth";
import {uploader_name, uploader_pw} from "./Cypress";
import {generateDummyCompanyInformation, uploadCompanyViaApi} from "./CompanyUpload";
import {FixtureData} from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

export async function uploadOneLksgDatasetViaApi(
  token: string,
  companyId: string,
  data: LksgData
): Promise<DataMetaInformation> {
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedLksgData({
    companyId,
    data,
  });
  return response.data;
}

export function getPreparedLksgFixture(companyName: string, preparedFixtures: Array<FixtureData<LksgData>>): FixtureData<LksgData> {
  const preparedFixture = preparedFixtures.find(
      (fixtureData): boolean => fixtureData.companyInformation.companyName == companyName
  )!;
  if (!preparedFixture) {
    throw new ReferenceError(
        "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
    );
  } else {
    return preparedFixture;
  }
} // TODO this is partially a duplicate in all DataIntegrity tests

export function uploadCompanyAndLksgDataViaApi(
    companyInformation: CompanyInformation,
    testData: LksgData
): Chainable<UploadIds> {
  return getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
        (storedCompany) => {
          return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, testData).then((dataMetaInformation) => {
            return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
          });
        }
    );
  }); // TODO might be that the companyId is sufficient as returned in Chainable
} // TODO might be kind of a duplicate for all Dataintegrity tests!

export async function getReportingYearOfLksgDataSet(dataId: string, token: string): Promise<string> {
  // check if lksg
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedLksgData(dataId);
  const lksgData = response.data.data;
  const reportingDate = lksgData!.social!.general!.dataDate; // TODO
  if (lksgData) {
    return reportingDate!.split("-").shift()!; // TODO
  } else {
    throw Error(`blub`);
  }
}
