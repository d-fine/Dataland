import { Configuration, DataTypeEnum } from "@clients/backend";
import { getUnifiedFrameworkDataControllerFromConfiguration } from "@/utils/api/FrameworkApiClient";

describe("It should be possible to construct Unified API Clients for every framework", () => {
  for (const frameworkIdentifier of Object.keys(DataTypeEnum)) {
    const frameworkDataType = DataTypeEnum[frameworkIdentifier as keyof typeof DataTypeEnum];

    it(`Should be possible to construct Unified API Clients for the ${frameworkIdentifier} framework and request data using them`, () => {
      const frameworkApiClient = getUnifiedFrameworkDataControllerFromConfiguration(
        frameworkDataType,
        new Configuration(),
      );

      cy.intercept(
        {
          method: "GET",
          url: `/api/data/${frameworkDataType}/test-data-id`,
        },
        {},
      ).as("api-client-test-request");

      void frameworkApiClient.getFrameworkData("test-data-id");

      cy.wait("@api-client-test-request");
    });
  }
});
