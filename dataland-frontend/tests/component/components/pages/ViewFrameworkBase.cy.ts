import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { DataMetaInformation, QAStatus, DataTypeEnum } from "@clients/backend";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { shallowMount } from "@vue/test-utils";
import { nextTick } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { minimalKeycloakMock } from "@ct/testUtils/keycloak";

describe("Component test for ViewFrameworkBase", () => {
  it("Should display only accepted datasets", () => {
    const approvedDatasetId = "dataset-1";
    cy.intercept("**/api/metadata*", {
      statusCode: 200,
      body: [
        {
          dataId: approvedDatasetId,
          qaStatus: QAStatus.Accepted,
        },
        {
          dataId: "dataset-2",
          qaStatus: QAStatus.Pending,
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
      expect(firstUpdateDataIdEmit).to.have.length(1);
      expect(firstUpdateDataIdEmit[0]).to.equal(approvedDatasetId);
    });
  });

  it("Should proper set the component based on data", () => {
    cy.fixture("MetaInfoDataForCompany.json").then((newData) => {
      cy.intercept("**/api/metadata*", {
        statusCode: 200,
        body: newData as Array<DataMetaInformation>,
      }).as("inter");

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
        data() {
          return {
            dataTypesInDropdown: [] as { label: string; value: string }[],
            mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>(),
          };
        },
        props: {
          dataType: DataTypeEnum.EutaxonomyFinancials,
          companyId: "86226010-some-fake-dataId-52824875a3e7",
        },
      });
      cy.wait("@inter").then(() => {
        expect(wrapper.vm.dataTypesInDropdown).to.be.an("array").that.is.not.empty;
        expect(wrapper.vm.dataTypesInDropdown).to.deep.equal([
          { label: humanizeString(DataTypeEnum.EutaxonomyFinancials), value: DataTypeEnum.EutaxonomyFinancials },
          {
            label: humanizeString(DataTypeEnum.EutaxonomyNonFinancials),
            value: DataTypeEnum.EutaxonomyNonFinancials,
          },
          { label: humanizeString(DataTypeEnum.Lksg), value: DataTypeEnum.Lksg },
          { label: humanizeString(DataTypeEnum.Sfdr), value: DataTypeEnum.Sfdr },
        ]);
      });

      cy.fixture("MapsForReportingsPeriodForDifferentDatasetAsArrays.json").then(
        async (data: { "eutaxonomy-financials": []; lksg: [] }) => {
          expect(Array.from(wrapper.vm.mapOfReportingPeriodToActiveDataset)).to.deep.equal(
            data[DataTypeEnum.EutaxonomyFinancials] as []
          );
          await wrapper.setProps({ dataType: DataTypeEnum.Lksg });
          await nextTick();
          expect(wrapper.props("dataType")).to.eq(DataTypeEnum.Lksg);
          await nextTick(() => {
            cy.wait(1000).then(() => {
              expect(Array.from(wrapper.vm.mapOfReportingPeriodToActiveDataset)).to.deep.equal(data[DataTypeEnum.Lksg]);
            });
          });
        }
      );
    });
  });

  it("Should not display the edit and create new dataset button on the framework view page for a data reader", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept("**/api/metadata**", []);
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: keycloakMock,
      global: {
        stubs: ["CompanyInformation"],
      },
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        dataType: DataTypeEnum.Lksg,
        companyID: "mock-company-id",
      });
      cy.get("button[data-test=editDatasetButton]").should("not.exist");
      cy.get("a[data-test=gotoNewDatasetButton]").should("not.exist");
    });
  });

  it(
    "Should display the edit and crate new dataset button for users with " +
      "upload permission and framework with edit page",
    () => {
      const keycloakMock = minimalKeycloakMock({
        roles: ["ROLE_USER", "ROLE_UPLOADER"],
      });
      cy.intercept("**/api/metadata**", []);
      cy.mountWithPlugins(ViewFrameworkBase, {
        keycloak: keycloakMock,
        global: {
          stubs: ["CompanyInformation"],
        },
      }).then((mounted) => {
        void mounted.wrapper.setProps({
          dataType: DataTypeEnum.Lksg,
          companyID: "mock-company-id",
        });
        cy.get("a[data-test=gotoNewDatasetButton] > button").should("exist");
        cy.get("button[data-test=editDatasetButton]").should("exist");
      });
    }
  );

  it(
    "Should display the add new dataset button, but not the edit button " +
      "on framework-view-pages for which no edit functionality has been implemented",
    () => {
      const keycloakMock = minimalKeycloakMock({
        roles: ["ROLE_USER", "ROLE_UPLOADER"],
      });
      cy.intercept("**/api/metadata**", []);
      cy.mountWithPlugins(ViewFrameworkBase, {
        keycloak: keycloakMock,
        global: {
          stubs: ["CompanyInformation"],
        },
      }).then((mounted) => {
        void mounted.wrapper.setProps({
          dataType: DataTypeEnum.Sfdr,
          companyID: "mock-company-id",
        });
        cy.get("a[data-test=gotoNewDatasetButton] > button").should("exist");
        cy.get("a[data-test=gotoNewDatasetButton]").should(
          "have.attr",
          "href",
          "/companies/mock-company-id/frameworks/upload"
        );
        cy.get("button[data-test=editDatasetButton]").should("not.exist");
      });
    }
  );
});
