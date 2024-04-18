import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
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
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Open,
      "2021",
      DataTypeEnum.Lksg,
      "Mock-Company-Id",
    ),
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      "2022",
      DataTypeEnum.Lksg,
      "Mock-Company-Id",
    ),
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      "2024",
      DataTypeEnum.Lksg,
      "Mock-Company-Id",
    ),
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      "1996",
      DataTypeEnum.EutaxonomyNonFinancials,
      "550e8400-e29b-11d4-a716-446655440000",
    ),
  );
  return storedDataRequests;
}

/**
 * Creates a default stored data request
 * @returns The request
 */
export function generateStoredDataRequest(): StoredDataRequest {
  const minimalNumberOfMessageObjects = 1;
  const messageHistory = generateArray(() => generateStoredDataRequestMessage(), minimalNumberOfMessageObjects);
  const timeOffsetBetweenCreationAndLastModified = 500;
  return {
    dataRequestId: faker.string.uuid(),
    userId: faker.string.uuid(),
    creationTimestamp: generateInt(timeOffsetBetweenCreationAndLastModified),
    dataType: pickOneElement(Object.values(DataTypeEnum)),
    reportingPeriod: generateReportingPeriod(),
    datalandCompanyId: faker.string.uuid(),
    messageHistory: messageHistory,
    lastModifiedDate: generateInt(500) + timeOffsetBetweenCreationAndLastModified,
    requestStatus: pickOneElement(Object.values(RequestStatus)),
  };
}
/**
 * Manipulates the request
 * @param input request to be manipulated
 * @param requestStatus the desired status
 * @param reportingPeriod the desired reporting period
 * @param dataType the desired framework
 * @param companyId the desired company id
 * @returns The manipulated request
 */
export function manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
  input: StoredDataRequest,
  requestStatus?: RequestStatus,
  reportingPeriod?: string,
  dataType?: DataTypeEnum,
  companyId?: string,
): StoredDataRequest {
  input.requestStatus = requestStatus ?? input.requestStatus;
  input.reportingPeriod = reportingPeriod ?? input.reportingPeriod;
  input.dataType = dataType ?? input.dataType;
  input.datalandCompanyId = companyId ?? input.datalandCompanyId;
  return input;
}
