import {visitAndCheckAppMount} from "../../../support/commands";

describe('Back Button test suite', () => {
    it('company eu taxonomy page should be present and contain back button', function () {
        cy.restoreLoginSession()
        visitAndCheckAppMount("/searchtaxonomy");
        cy.get('h1').should("contain", "Search EU Taxonomy data")
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            visitAndCheckAppMount("/companies/" + dataIdList[5] + "/eutaxonomies");
            cy.get('span.text-primary[title=back_button]')
                .parent('.cursor-pointer.grid.align-items-center')
                .click()
                .url().should('include', '/searchtaxonomy')
        });
    });
})


