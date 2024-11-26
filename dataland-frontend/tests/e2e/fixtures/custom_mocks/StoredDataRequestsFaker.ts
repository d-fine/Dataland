import { AccessStatus, RequestPriority, RequestStatus, type StoredDataRequest } from '@clients/communitymanager';
import { DataTypeEnum } from '@clients/backend';
import { generateInt } from '@e2e/fixtures/common/NumberFixtures';
import { generateReportingPeriod } from '@e2e/fixtures/common/ReportingPeriodFixtures';
import { generateStoredDataRequestMessage } from '@e2e/fixtures/custom_mocks/StoredDataRequestMessageFaker';
import { faker } from '@faker-js/faker';
import { generateArray, pickOneElement } from '@e2e/fixtures/FixtureUtils';

const DEFAULT_TIME_OFFSET = 500;

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
      undefined,
      '2021',
      DataTypeEnum.Lksg,
      'Mock-Company-Id'
    )
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      undefined,
      '2022',
      DataTypeEnum.Lksg,
      'Mock-Company-Id'
    )
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      undefined,
      '2024',
      DataTypeEnum.Lksg,
      'Mock-Company-Id'
    )
  );
  storedDataRequests.push(
    manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
      generateStoredDataRequest(),
      RequestStatus.Answered,
      undefined,
      '1996',
      DataTypeEnum.EutaxonomyNonFinancials,
      '550e8400-e29b-11d4-a716-446655440000'
    )
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
  const timeOffsetBetweenCreationAndLastModified = DEFAULT_TIME_OFFSET;
  const creationTime = generateInt(timeOffsetBetweenCreationAndLastModified);
  const lastModifiedTime = generateInt(DEFAULT_TIME_OFFSET) + timeOffsetBetweenCreationAndLastModified;
  const status = pickOneElement(Object.values(RequestStatus));
  const accessStatus = pickOneElement(Object.values(AccessStatus));
  const requestStatusHistory = [
    {
      status: RequestStatus.Open,
      creationTimestamp: creationTime,
      accessStatus: AccessStatus.Public,
    },
    {
      status: status,
      creationTimestamp: lastModifiedTime,
      accessStatus: accessStatus,
    },
  ];
  return {
    dataRequestId: faker.string.uuid(),
    userId: faker.string.uuid(),
    creationTimestamp: creationTime,
    dataType: pickOneElement(Object.values(DataTypeEnum)),
    reportingPeriod: generateReportingPeriod(),
    datalandCompanyId: faker.string.uuid(),
    messageHistory: messageHistory,
    dataRequestStatusHistory: requestStatusHistory,
    lastModifiedDate: lastModifiedTime,
    requestStatus: status,
    accessStatus: accessStatus,
    requestPriority: RequestPriority.Normal,
  };
}
/**
 * Manipulates the request
 * @param input request to be manipulated
 * @param requestStatus the desired status
 * @param accessStatus the desired access status
 * @param reportingPeriod the desired reporting period
 * @param dataType the desired framework
 * @param companyId the desired company id
 * @returns The manipulated request
 */
export function manipulateFixtureToHaveStatusReportingPeriodDataTypeCompanyId(
  input: StoredDataRequest,
  requestStatus?: RequestStatus,
  accessStatus?: AccessStatus,
  reportingPeriod?: string,
  dataType?: DataTypeEnum,
  companyId?: string
): StoredDataRequest {
  input.requestStatus = requestStatus ?? input.requestStatus;
  input.accessStatus = accessStatus ?? input.accessStatus;
  input.reportingPeriod = reportingPeriod ?? input.reportingPeriod;
  input.dataType = dataType ?? input.dataType;
  input.datalandCompanyId = companyId ?? input.datalandCompanyId;
  return input;
}
