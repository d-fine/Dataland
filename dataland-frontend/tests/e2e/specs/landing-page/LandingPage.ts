describe("Check that the Landing Page to work properly", () => {
  it("Check the links and buttons", () => {
    cy.intercept({ url: "https://www.youtube.com/**" }, { forceNetworkError: false }).as("youtube");
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
    cy.get("h2:contains('Impressum')");
    cy.get("a[href='/']").click();

    cy.get("a[href='/dataprivacy']").click();
    cy.get("h1:contains('Datenschutzhinweise')");
    cy.get("a[href='/']").click();

    cy.get("a[href='/terms']").click();
    cy.get("h1:contains('Allgemeine Bedingungen für die Teilnahme an Dataland')");
    cy.get("a[href='/']").click();
  });
});
