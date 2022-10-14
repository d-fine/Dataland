import "@cypress/code-coverage/support";
import "./Commands";
import { interceptAllAndCheckFor500Errors } from "../utils/ApiUtils";

beforeEach(() => {
  interceptAllAndCheckFor500Errors();
});
