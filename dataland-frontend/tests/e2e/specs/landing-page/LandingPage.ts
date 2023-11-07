import {describeIf} from "@e2e/support/TestUtility";

// describe("Check that the Landing Page to work properly", () => {
  describe("Overall landing page tests", () => {
    it("Check the links and buttons", () => {
      cy.intercept({url: "https://www.youtube.com/**"}, {forceNetworkError: false}).as("youtube");
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
describeIf(
    "Execute the test with clicking on the decline button on the cookie banner",
    {
      executionEnvironments: ["developmentLocal"],
    },
    () => {
  it("Check if vertical scroll is locked when dragging horizontally", () => {
    performScrollingTest(true);
  });
    },
);

  describeIf(
      "Execute the test without clicking on the nonexistent cookie banner",
      {
        executionEnvironments: ["ci", "developmentCd", "previewCd"],
      },
      () => {
        it("Check if vertical scroll is locked when dragging horizontally", () => {
          performScrollingTest(false)
        });
      },
  );

  function performScrollingTest(clickBanner: boolean) {
    cy.viewport(400, 800);
    cy.visitAndCheckAppMount("/");
    if (clickBanner) {
      cy.get('#CybotCookiebotDialogBodyButtonDecline').click();
    }
      cy.window().then((win) => {
          const scrollYBeforeScrolling = win.scrollY;
      cy.get('[data-test="howitworks"]')
        .scrollIntoView()
        .trigger("pointerdown", 100, 400, { button: 0 })
        .trigger("pointermove", {eventConstructor: "MouseEvent", clientX: 200, clientY: 440,})
        .trigger("pointermove", {eventConstructor: "MouseEvent", clientX: 300, clientY: 340,})
        .trigger("pointermove", {eventConstructor: "MouseEvent", clientX: 50, clientY: 440,})
        .trigger("pointerup", { button: 0 });
        const scrollYAfterScrolling = win.scrollY;
          expect(scrollYBeforeScrolling).equals(scrollYAfterScrolling);
    });
  }
// });
