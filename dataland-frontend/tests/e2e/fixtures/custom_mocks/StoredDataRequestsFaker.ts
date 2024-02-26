import { DataRequestCompanyIdentifierType, RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import { DataTypeEnum } from "@clients/backend";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateStoredDataRequestMessage } from "@e2e/fixtures/custom_mocks/StoredDataRequestMessageFaker";
import { faker } from "@faker-js/faker";
import { generateArray, pickOneElement } from "@e2e/fixtures/FixtureUtils";

/**
 * Creates a list of stored data requests
 * @returns The list of request
 */
export function generateStoredDataRequests(): StoredDataRequest[] {
  const storedDataRequests = [];
  storedDataRequests.push(generateStoredDataRequest());
  storedDataRequests.push(
    manipulateFixtureToHaveDataType(
      manipulateFixtureToHaveCompanyId(
        manipulateFixtureToHaveReportingPeriod(
          manipulateFixtureToHaveStatus(generateStoredDataRequest(), RequestStatus.Open),
          "2021",
        ),
        "Mock-Company-Id",
      ),
      DataTypeEnum.Lksg,
    ),
  );
  storedDataRequests.push(
    manipulateFixtureToHaveDataType(
      manipulateFixtureToHaveCompanyId(
        manipulateFixtureToHaveReportingPeriod(
          manipulateFixtureToHaveStatus(generateStoredDataRequest(), RequestStatus.Answered),
          "2022",
        ),
        "Mock-Company-Id",
      ),
      DataTypeEnum.Lksg,
    ),
  );
  storedDataRequests.push(
    manipulateFixtureToHaveDataType(
      manipulateFixtureToHaveCompanyId(
        manipulateFixtureToHaveReportingPeriod(
          manipulateFixtureToHaveStatus(generateStoredDataRequest(), RequestStatus.Answered),
          "2024",
        ),
        "Mock-Company-Id",
      ),
      DataTypeEnum.Lksg,
    ),
  );
  return storedDataRequests;
}

/**
 * Creates a default stored data request
 * @returns The request
 */
function generateStoredDataRequest(): StoredDataRequest {
  const messageHistory = generateArray(() => generateStoredDataRequestMessage(), 1);
  return {
    dataRequestId: faker.string.uuid(),
    userId: faker.string.uuid(),
    creationTimestamp: generateInt(500),
    dataType: pickOneElement(Object.values(DataTypeEnum)),
    reportingPeriod: generateReportingPeriod(),
    dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType.DatalandCompanyId,
    dataRequestCompanyIdentifierValue: faker.string.uuid(),
    messageHistory: messageHistory,
    lastModifiedDate: generateInt(500) + 500,
    requestStatus: pickOneElement(Object.values(RequestStatus)),
  };
}
/**
 * Sets the requests status to the desired status
 * @param input request to be manipulated
 * @param requestStatus the desired status
 * @returns The manipulated request
 */
function manipulateFixtureToHaveStatus(input: StoredDataRequest, requestStatus: RequestStatus): StoredDataRequest {
  input.requestStatus = requestStatus;
  return input;
}
/**
 * Sets the requests reporting period to the desired string
 * @param input request to be manipulated
 * @param reportingPeriod new reporting period
 * @returns The manipulated request
 */
function manipulateFixtureToHaveReportingPeriod(input: StoredDataRequest, reportingPeriod: string): StoredDataRequest {
  input.reportingPeriod = reportingPeriod;
  return input;
}

/**
 * Sets the requests framework to the desired framework
 * @param input request to be manipulated
 * @param dataType new framework
 * @returns The manipulated request
 */
function manipulateFixtureToHaveDataType(input: StoredDataRequest, dataType: DataTypeEnum): StoredDataRequest {
  input.dataType = dataType;
  return input;
}
/**
 * Sets the requests companyId to the desired string
 * @param input request to be manipulated
 * @param companyId new company Id (dataland company Id)
 * @returns The manipulated request
 */
function manipulateFixtureToHaveCompanyId(input: StoredDataRequest, companyId: string): StoredDataRequest {
  input.dataRequestCompanyIdentifierType = DataRequestCompanyIdentifierType.DatalandCompanyId;
  input.dataRequestCompanyIdentifierValue = companyId;
  return input;
}
