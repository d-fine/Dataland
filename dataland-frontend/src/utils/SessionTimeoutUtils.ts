import {
  TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS,
  TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS,
} from "@/utils/Constants";
import { loginAndRedirectToSearchPage, logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import Keycloak from "keycloak-js";
import { useSessionStateStore } from "@/stores/stores";

const minRequiredValidityTimeOfRefreshTokenDuringCheck = TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS + 1000;

/**
 * Updates the timestamp for the session warning to the current timestamp plus the amount of time that should be allowed
 * to pass until the warning appears.
 *
 * @param keycloak TODO
 */
export function updateSessionWarningTimestamp(keycloak: Keycloak): void {
  console.log("setting new timestamps"); // TODO debugging
  if (keycloak.refreshTokenParsed?.exp) {
    const expiryTimestampOfCurrentRefreshTokenInMs = keycloak.refreshTokenParsed?.exp * 1000;
    useSessionStateStore().sessionWarningTimestampInMs =
      expiryTimestampOfCurrentRefreshTokenInMs - TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS;
  }
}

/**
 * Gets the current session warning timestamp.
 *
 * @returns the parsed session warning timestamp
 */
function getSessionWarningTimestamp(): number {
  const sessionWarningTimestampInMs = useSessionStateStore().sessionWarningTimestampInMs;
  if (sessionWarningTimestampInMs) {
    return sessionWarningTimestampInMs;
  } else {
    throw Error(`Required timestamp for the session warning could not be found. 
    This is not acceptable for running Dataland.`);
  }
}

/**
 * Starts the setInterval-method which in fixed time intervals checks if the session warning timestamp was surpassed.
 * If it is surpassed, the provided callback function will be executed.
 * The function ID of this setInterval-method is stored inside the window storage by using pinia.
 *
 * @param keycloak is the keycloak adaptor used to do a logout in case the session warning timestamp cannot be retrieved
 * @param onSurpassingExpiredSessionTimestampCallback is the callback function which will be executed as soon as the
 * session warning timestamp is surpassed
 */
function startSessionSetIntervalFunction(
  keycloak: Keycloak,
  onSurpassingExpiredSessionTimestampCallback: () => void
): void {
  console.log("starting set interval from scratch"); //TODO debugging
  const functionId = setInterval(() => {
    console.log("setInterval is running once"); // TODO debugging
    const currentTimestampInMs = new Date().getTime();
    const sessionWarningTimestamp = getSessionWarningTimestamp();
    if (!sessionWarningTimestamp) {
      logoutAndRedirectToUri(keycloak, ""); // TODO give a reason here for the logout? something like: due to an error
    } else {
      if (currentTimestampInMs >= sessionWarningTimestamp) {
        console.log("You have passed the logout timestamp. You'll get a session expired popup now."); // TODO debugging
        clearInterval(functionId);
        onSurpassingExpiredSessionTimestampCallback();
      } else {
        console.log("You have not reached the logoutTimeStamp in the local storage yet. You stay logged in"); // TODO debugging
      }
    }
  }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
}

export function isCurrentRefreshTokenExpired(keycloak: Keycloak): boolean {
  const currentTimestamp = new Date().getTime();
  if (keycloak.refreshTokenParsed?.exp) {
    const expiryTimestampOfCurrentRefreshTokenInMs = keycloak.refreshTokenParsed?.exp * 1000;
    console.log(
      "currentTime: " +
        currentTimestamp.toString() +
        " expiryTimestampOfCurrentRefreshToken: " +
        expiryTimestampOfCurrentRefreshTokenInMs
    ); // TODO debugging
    return (
      currentTimestamp + minRequiredValidityTimeOfRefreshTokenDuringCheck > expiryTimestampOfCurrentRefreshTokenInMs
    );
  } else {
    throw Error("The refresh token cannot be parsed. This is not acceptable for running Dataland.");
  }
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
  const sessionStateStore = useSessionStateStore();
  const isRefreshTokenExpired = sessionStateStore.isRefreshTokenExpired;
  // TODO comment why this is necessary (grace time problem)
  if (isRefreshTokenExpired) {
    console.log("refreshing session => refresh Token expired => logout"); // TODO debugging
    loginAndRedirectToSearchPage(keycloak);
  } else {
    console.log("refreshing session => refresh Token still valid => updating tokens"); // TODO debugging
    keycloak.updateToken(-1).catch(() => {
      console.log(`Could not refresh token`);
    }); // token => share storage
    sessionStateStore.isRefreshTokenExpired = false;
    updateSessionWarningTimestamp(keycloak);
    startSessionSetIntervalFunction(keycloak, onSurpassingExpiredSessionTimestampCallback);
  }
}
