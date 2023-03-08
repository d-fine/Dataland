import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { DataMetaInformation, QAStatus } from "@clients/backend";
import Keycloak from "keycloak-js";
import { assertDefined } from "../../../../src/utils/TypeScriptUtils";
import { shallowMount } from "@vue/test-utils";
import { waitForComponent } from "../../utils/helper.cy";
import { nextTick } from "vue";
import { DataTypeEnum } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";

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

  it("Should get the data and proper set the component based on it", () => {
    cy.fixture("MapsForReportingsPeriodForDifferentDatasetAsArrays.json").as("MapsForReportings");
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

    cy.fixture("MetaInfoDataForCompany.json").then((newData) => {
      cy.intercept("**/api/metadata*", {
        statusCode: 200,
        body: newData,
      }).as("listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod");
    });

    void waitForComponent(wrapper);

    cy.wait("@listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod").then(() => {
      expect(wrapper.vm.$data.dataTypesInDropdown).to.deep.equal([
        { label: humanizeString(DataTypeEnum.EutaxonomyFinancials), value: DataTypeEnum.EutaxonomyFinancials },
        { label: humanizeString(DataTypeEnum.EutaxonomyNonFinancials), value: DataTypeEnum.EutaxonomyNonFinancials },
        { label: humanizeString(DataTypeEnum.Lksg), value: DataTypeEnum.Lksg },
        { label: humanizeString(DataTypeEnum.Sfdr), value: DataTypeEnum.Sfdr },
      ]);

      cy.fixture("MapsForReportingsPeriodForDifferentDatasetAsArrays.json").then(async (data) => {
        expect(Array.from(wrapper.vm.$data.mapOfReportingPeriodToActiveDataset)).to.deep.equal(
          data[`${wrapper.vm.$props.dataType}`]
        );
        await wrapper.setProps({ dataType: DataTypeEnum.Lksg });
        await nextTick();
        expect(wrapper.props("dataType")).to.eq(DataTypeEnum.Lksg);
        await nextTick(() => {
          cy.wait(1000).then(() => {
            expect(Array.from(wrapper.vm.$data.mapOfReportingPeriodToActiveDataset)).to.deep.equal(
              data[`${wrapper.vm.$props.dataType}`]
            );
          });
        });
      });
    });
  });
});
