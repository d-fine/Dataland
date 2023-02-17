import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { DatasetQualityStatus } from "@clients/backend";
import Keycloak from "keycloak-js";
import { assertDefined } from "../../../../src/utils/TypeScriptUtils";
import { shallowMount } from "@vue/test-utils";

describe("Component test for ViewFrameworkBase", () => {
  it("Should display only accepted datasets", () => {
    cy.intercept("**/api/metadata*", {
      statusCode: 200,
      body: [
        {
          dataId: "1",
          qualityStatus: DatasetQualityStatus.Accepted,
        },
        {
          dataId: "2",
          qualityStatus: DatasetQualityStatus.Pending,
        },
      ],
    });
    const wrapper = shallowMount(ViewFrameworkBase, {
      global: {
        provide: {
          authenticated: true,
          getKeycloakPromise() {
            return Promise.resolve({
              authenticated: true,
            });
          },
        },
      },
      setup() {
        return {
          getKeycloakPromise: (): Promise<Keycloak> => {
            return Promise.resolve({} as Keycloak);
          },
        };
      },
      props: {
        companyID: "",
      },
    });
    cy.wait(1000).then(() => {
      const firstUpdateDataIdEmit = (assertDefined(wrapper.emitted().updateDataId) as string[][][])[0][0];
      expect(firstUpdateDataIdEmit.length).to.equal(1);
      expect(firstUpdateDataIdEmit[0]).to.equal("1");
    });
  });
});
