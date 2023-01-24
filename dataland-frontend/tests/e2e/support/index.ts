import "@cypress/code-coverage/support";
import "./Commands";
import { interceptAllAndCheckFor500Errors } from "@e2e/utils/GeneralApiUtils";

beforeEach(() => {
  interceptAllAndCheckFor500Errors();
});
