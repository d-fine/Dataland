import { login } from "@e2e/utils/Auth";
import { reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";

describe("As a user, I expect the Quality Assurance link in the profile dropdown to be hidden or visible depending on my role", () => {
  const profileDropdownToggleSelector = "div[id='profile-picture-dropdown-toggle']";
  const qaAnchorSelector = "a[id='profile-picture-dropdown-qa-services-anchor']";

  it("Check wether the Quality Assurance link is visible when the user has the appropriate role", () => {
    login(uploader_name, uploader_pw);
    cy.get(profileDropdownToggleSelector).click().wait(1000).get(qaAnchorSelector).should("exist").should("be.visible");
  });

  it("Check wether the Quality Assurance link is not visible when the user does not have the appropriate role", () => {
    login(reader_name, reader_pw);
    cy.get(profileDropdownToggleSelector).click().wait(1000).get(qaAnchorSelector).should("not.exist");
  });
});
