import ReviewRequestButtonsComponent from "@/components/resources/ReviewRequestButtons.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { DataTypeEnum } from "../../../../../build/clients/backend";
describe("Component tests for the data request review buttons", function (): void {
  it("Check reopen functionality", function () {
    cy.mountWithPlugins(ReviewRequestButtonsComponent, {
      keycloak: minimalKeycloakMock({}),
      props: {
        companyId: "",
        framework: DataTypeEnum.Lksg,
      },
    }).then(() => {});
  });
});
