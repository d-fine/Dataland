/**
 * As a user, I expect that proper authentication and security procedures are in place that work like I expect them to
 */
describe("User Authentication Tests", () => {
  require("./Login");
  require("./Register");
  require("./VisitPagesUnauth");
  require("./LandingPageRedirect");
  require("./VerifyPkceFlow");
  require("./VerifyEmailLogoExists");
  require("./SessionTimeout");
});
