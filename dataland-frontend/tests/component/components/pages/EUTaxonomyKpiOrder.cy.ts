import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { NewEuTaxonomyForNonFinancialsPanel } from "@/components/generics/ViewMultipleDatasetsDisplayBase"
describe("Component test for the NewEUTaxonomy Page", () => {
    before(() => {
        cy.mountWithPlugins(NewEuTaxonomyForNonFinancialsPanel, {
            keycloak: minimalKeycloakMock({
                roles: ["ROLE_USER", "ROLE_UPLOADER"],
                userId: "Mock-User-Id",
            }),
        })
    });


    const kpiList:string[] = ["Basic Information", "Assurance", "Total Revenue", "Total CapEx", "Total OpEx"];
    it("Check order of the displayed KPIs", () => {
        cy.get(".p-datatable-tbody").find("p-rowgroup-header")
            .each((element, index) => {
                element.should('have.text', kpiList[index]);
            });
    });



});



