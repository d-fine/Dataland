import { TIME_UNTIL_SESSION_WARNING_IN_MS, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import Keycloak from "keycloak-js";
import { useFunctionIdsStore } from "@/stores/stores";

const keyForSessionWarningTimestampInLocalStorage = "sessionWarningTimestamp";

/**
 * Updates the timestamp for the session warning to the current timestamp plus the amount of time that should be allowed
 * to pass until the warning appears. Then it stores it in the local storage of the browser.
 */
export function updateSessionWarningTimestampInLocalStorage(): void {
  console.log("setting new timestamps"); // TODO debugging
  const currentTimeInMs = new Date().getTime();
  const sessionWarningTimestampInMs = currentTimeInMs + TIME_UNTIL_SESSION_WARNING_IN_MS;
  localStorage.setItem(keyForSessionWarningTimestampInLocalStorage, sessionWarningTimestampInMs.toString());
}

/**
 * Gets the session warning timestamp from the local storage of the browser, parses it as number and return ist.
 *
 * @returns the parsed session warning timestamp
 */
function getSessionWarningTimestampFromLocalStorage(): number {
  const sessionWarningTimestampInLocalStorage = localStorage.getItem(keyForSessionWarningTimestampInLocalStorage);
  if (sessionWarningTimestampInLocalStorage) {
    return parseInt(sessionWarningTimestampInLocalStorage);
  } else {
    throw Error(`Required timestamp for the session warning could not be found in the local storage of the browser" +
        "by using the key ${keyForSessionWarningTimestampInLocalStorage}.`);
  }
}

/**
 * Starts the setInterval-method which in fixed time intervals checks if the session warning timestamp was surpassed.
 * If it is surpassed, the provided callback function will be executed.
 * The function ID of this setInterval-method is stored inside the window storage with by using pinia.
 *
 * @param keycloak is the keycloak adaptor used to do a logout in case the session warning timestamp cannot be retrieved
 * from the local storage
 * @param onSurpassingExpiredSessionTimestampCallback is the callback function which will be executed as soon as the
 * session warning timestamp is surpassed
 */
function startSessionSetIntervalFunction(
  keycloak: Keycloak,
  onSurpassingExpiredSessionTimestampCallback: () => void
): void {
  console.log("starting set interval from scratch"); //TODO
  useFunctionIdsStore().sessionCheckSetIntervalFunctionId = setInterval(() => {
    console.log("setInterval is running once"); // TODO debugging
    const currentTimestampInMs = new Date().getTime();
    const sessionWarningTimestamp = getSessionWarningTimestampFromLocalStorage();
    if (!sessionWarningTimestamp) {
      logoutAndRedirectToUri(keycloak, ""); // TODO give a reason here for the logout? something like: due to an error
    } else {
      if (currentTimestampInMs >= sessionWarningTimestamp) {
        console.log("You have passed the logout timestamp. You'll get a session expired popup now."); // TODO debugging
        onSurpassingExpiredSessionTimestampCallback();
      } else {
        console.log("You have not reached the logoutTimeStamp in the local storage yet. You stay logged in"); // TODO debugging
      }
    }
  }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
}

/**
 * Gets the function ID of the setInterval-method which continuously checks if the session warning timestamp is
 * surpassed to then stop it.
 */
export function clearSessionSetIntervalFunction(): void {
  console.log("stopping setInterval"); // TODO debugging
  clearInterval(useFunctionIdsStore().sessionCheckSetIntervalFunctionId);
}

/**
 * Tries to refresh a session. If the refresh token is still valid, it refreshes the session by getting fresh access
 * and refresh tokens from keycloak, updating the session warning timestamp and re-starting the setInterval-method
 * which continuously checks if the session warning timestamp is surpassed.
 * If the refresh token is already expired it logs the user out to finally close the session.
 *
 * @param keycloak is the keycloak adaptor used to do a logout in case the refresh token is invalid, and to update the tokens
 * in case the refresh token is still valid
 * @param onSurpassingExpiredSessionTimestampCallback is the callback function which will be executed as soon as the
 * setInterval method detects that session warning timestamp is surpassed
 */
export function tryToRefreshSession(keycloak: Keycloak, onSurpassingExpiredSessionTimestampCallback: () => void): void {
  console.log("refreshing session"); // TODO debugging
  const currentTimestamp = new Date().getTime();
  const maxTokenValidity = 600; // TODO discuss/adjust
  const bufferUntilRefreshTokenExpiryInMs = 3000;
  let expiryTimestampOfCurrentRefreshTokenInMs;
  if (keycloak.refreshTokenParsed?.exp) {
    expiryTimestampOfCurrentRefreshTokenInMs = keycloak.refreshTokenParsed?.exp * 1000;
  }
  console.log(
    "currentTime: " + currentTimestamp.toString() + " expiryTimestampOfCurrentRefreshToken: " + expiryTimestampOfCurrentRefreshTokenInMs
  ); // TODO debugging
  if (expiryTimestampOfCurrentRefreshTokenInMs) {
    const isRefreshTokenStillValid =
      currentTimestamp < expiryTimestampOfCurrentRefreshTokenInMs - bufferUntilRefreshTokenExpiryInMs;
    // TODO comment why this is necessary (grace time problem)
    if (isRefreshTokenStillValid) {
      console.log("refreshing session => refresh Token still valid => updating tokens"); // TODO debugging
      keycloak.updateToken(maxTokenValidity);
      updateSessionWarningTimestampInLocalStorage();
      startSessionSetIntervalFunction(keycloak, onSurpassingExpiredSessionTimestampCallback);
    } else {
      console.log("refreshing session => refresh Token expired => logout"); // TODO debugging
      logoutAndRedirectToUri(keycloak, "?sessionClosed=true");
    }
  }
}
