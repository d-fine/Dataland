import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import MultiLayerDataTableFrameworkPanel from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { type DataMetaInformation } from "@clients/backend";
import { minimalKeycloakMock } from "./Keycloak";

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param frameworkIdentifier the identifier of the framework whose datasets are to be displayed
 * @param displayConfiguration the MLDT display configuration for the framework
 * @param datasetsToDisplay the datasets to mount
 * @param companyId the company ID of the mocked requests
 * @returns the component mounting chainable
 */
export function mountMLDTFrameworkPanelFromFakeFixture<Framework extends keyof FrameworkDataTypes>(
  frameworkIdentifier: Framework,
  displayConfiguration: MLDTConfig<FrameworkDataTypes[Framework]["data"]>,
  datasetsToDisplay: Array<FixtureData<FrameworkDataTypes[Framework]["data"]>>,
  companyId = "mock-company-id",
): Cypress.Chainable {
  const convertedDataAndMetaInformation: Array<DataAndMetaInformation<FrameworkDataTypes[Framework]["data"]>> =
    datasetsToDisplay.map((it, idx) => {
      const metaInfo: DataMetaInformation = {
        dataId: `data-id-${idx}`,
        companyId: companyId,
        dataType: frameworkIdentifier,
        uploadTime: 0,
        reportingPeriod: it.reportingPeriod,
        qaStatus: "Accepted",
        currentlyActive: true,
      };
      return {
        data: it.t,
        metaInfo: metaInfo,
      };
    });

  return mountMLDTFrameworkPanel(frameworkIdentifier, displayConfiguration, convertedDataAndMetaInformation, companyId);
}

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param frameworkIdentifier the identifier of the framework whose datasets are to be displayed
 * @param displayConfiguration the MLDT display configuration for the framework
 * @param datasetsToDisplay the datasets to mount
 * @param companyId the company ID of the mocked requests
 * @returns the component mounting chainable
 */
export function mountMLDTFrameworkPanel<Framework extends keyof FrameworkDataTypes>(
  frameworkIdentifier: Framework,
  displayConfiguration: MLDTConfig<FrameworkDataTypes[Framework]["data"]>,
  datasetsToDisplay: Array<DataAndMetaInformation<FrameworkDataTypes[Framework]["data"]>>,
  companyId = "mock-company-id",
): Cypress.Chainable {
  cy.intercept(`/api/data/${frameworkIdentifier}/companies/${companyId}`, datasetsToDisplay);
  return cy.mountWithDialog(
    MultiLayerDataTableFrameworkPanel,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      companyId: companyId,
      frameworkIdentifier: frameworkIdentifier,
      displayConfiguration: displayConfiguration,
    },
  );
}
