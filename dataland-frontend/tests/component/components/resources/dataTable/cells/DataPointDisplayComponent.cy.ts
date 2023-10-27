import {minimalKeycloakMock} from "../../../../testUtils/Keycloak";
import DataPointDisplayComponent from "@/components/resources/dataTable/cells/DataPointDisplayComponent.vue";
import {text} from "stream/consumers";

it("tests if link into position in text file works", () => {
    cy.mountWithDialog(
        DataPointDisplayComponent,
        {
            keycloak: minimalKeycloakMock({}),
        },
        { content: {
                displayValue: {
                    fieldLabel: "testingFieldLabel",
                    value: "testingValue",
                    dataSource: {
                        page: 5,
                        fileName: "testFileName",
                        fileReference: "fileReference"
                    }},
            } },
    ).then(() => {
    });
    //test if modal opens when link is clicked
    cy.get('a').click();
    cy.get('.p-dialog-header').should('exist');
    //test if required fields are present and filled with content
    cy.get(':nth-child(1)').should("contain.text","Value");
    cy.get('.p-datatable-body > :nth-child(1)').should("not.be.empty");
    cy.get(':nth-child(3)').should("contain.text","Data source"); //todo change to child 2 or 3, depending on presence
    cy.get('.p-dialog-content').should("contain","a");
    //test if optional fields are displayed only when content is present
});