import "@cypress/code-coverage/support";
import "./Commands";
import {interceptAllAndCheckFor500Errors, interceptAllDataPostsAndBypassQa} from "@e2e/utils/GeneralApiUtils";

beforeEach(() => {
  interceptAllAndCheckFor500Errors();
  interceptAllDataPostsAndBypassQa();
});
