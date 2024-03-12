import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import DataPointWrapperDisplayComponent from "@/components/resources/dataTable/cells/DataPointWrapperDisplayComponent.vue";
import { MLDTDisplayComponentName } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

it("tests if modal with link into position in text file works", () => {
  cy.mountWithPlugins(DataPointWrapperDisplayComponent, {
    keycloak: minimalKeycloakMock({}),
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    props: {
      content: {
        displayValue: {
          innerContents: {
            displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
            displayValue: "Yes",
          },
          value: "testingValue",
          dataSource: {
            fileName: "testFileName",
            fileReference: "fileReference",
          },
        },
      },
    },
  }).then(() => {});
  cy.get('[data-test="Report-Download-testFileName"]').should("exist").contains("Yes");
  cy.get('[data-test="download-icon"]').should("exist");
});
