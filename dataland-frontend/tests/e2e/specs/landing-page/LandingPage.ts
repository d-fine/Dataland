describe("Check that the Landing Page to work properly", () => {
  it("Check the links and buttons", () => {
    cy.visitAndCheckAppMount("/");

    cy.get("a:contains('Login')").click();
    cy.url().should("include", "/keycloak/realms/datalandsecurity/protocol/openid-connect/auth");
    cy.get("span:contains('HOME')").click();

    cy.get(`button[name="signup_dataland_button"]`).click();
    cy.url().should("include", "/keycloak/realms/datalandsecurity/protocol/openid-connect/registrations");
    cy.get("span:contains('HOME')").click();

    cy.get("button.quotes__button").click();
    cy.url().should("include", "/keycloak/realms/datalandsecurity/protocol/openid-connect/registrations");
    cy.get("span:contains('HOME')").click();

    cy.get("a[href='/imprint']").click();
    cy.get("h2:contains('Imprint')");
    cy.get("span:contains('BACK')").click();

    cy.get("a[href='/dataprivacy']").click();
    cy.get("h2:contains('Data Privacy')");
    cy.get("span:contains('BACK')").click();
  });
});
