import {
  TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS,
  TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS,
} from "@/utils/Constants";
import { loginAndRedirectToSearchPage, logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import Keycloak from "keycloak-js";
import { useFunctionIdsStore, useSessionStateStore } from "@/stores/stores";

const minRequiredValidityTimeOfRefreshTokenDuringCheck = TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS + 1000;

export function updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak: Keycloak) {
  keycloak
    .updateToken(-1) // TODO at then end of dev change this value
    .then(() => {
      const refreshTokenExpiryTime = keycloak.refreshTokenParsed?.exp
      if (refreshTokenExpiryTime) {
        useSessionStateStore().refreshToken = keycloak.refreshToken;
        useSessionStateStore().refreshTokenExpiryTimestampInMs = refreshTokenExpiryTime * 1000;
        useSessionStateStore().sessionWarningTimestampInMs = refreshTokenExpiryTime * 1000 - // TODO you could set this in the store itself
            TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS
        console.log("--------------NEW SESSION WARNING TIMESTAMP IS: " + refreshTokenExpiryTime * 1000)
      }
    })
    .catch(() => {
      console.log(`Could not refresh token`);
    });
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
export function startSessionSetIntervalFunction(
  keycloak: Keycloak,
  onSurpassingExpiredSessionTimestampCallback: () => void
): void {
  console.log("starting set interval from scratch"); //TODO debugging
  useFunctionIdsStore().functionIdOfSetIntervalForSessionWarning = setInterval(() => {
    console.log("setInterval is running once"); // TODO debugging
    const currentTimestampInMs = new Date().getTime();
    const sessionWarningTimestamp = getSessionWarningTimestamp();
    if (!sessionWarningTimestamp) {
      logoutAndRedirectToUri(keycloak, ""); // TODO give a reason here for the logout? something like: due to an error
    } else {
      if (currentTimestampInMs >= sessionWarningTimestamp) {
        console.log("You have passed the sessionWarningTimestamp timestamp. You'll get a session expired popup now."); // TODO debugging
        clearInterval(useFunctionIdsStore().functionIdOfSetIntervalForSessionWarning);
        onSurpassingExpiredSessionTimestampCallback();
      } else {
        console.log("You have not reached the sessionWarningTimestamp in the local storage yet. You stay logged in"); // TODO debugging
      }
    }
  }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
}

export function isCurrentRefreshTokenExpired(): boolean {
  const currentTimestamp = new Date().getTime();
  const expiryTimestampOfCurrentRefreshTokenInMs = useSessionStateStore().refreshTokenExpiryTimestampInMs
  if (expiryTimestampOfCurrentRefreshTokenInMs) {
    console.log(
      "currentTime: " +
        currentTimestamp.toString() +
        " expiryTimestampOfCurrentRefreshToken: " +
        expiryTimestampOfCurrentRefreshTokenInMs.toString()
    ); // TODO debugging
    return (
      currentTimestamp + minRequiredValidityTimeOfRefreshTokenDuringCheck > expiryTimestampOfCurrentRefreshTokenInMs
    );
  } else {
    throw Error("No expiry timestamp for the current refresh token could be found in the store. " +
        "This is not acceptable for running Dataland.");
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
 * setInterval method detects that session warning timestamp is surpassed
 */
export function tryToRefreshSession(keycloak: Keycloak): void {
  console.log("refreshing session"); // TODO debugging
  // TODO comment why this is necessary (grace time problem)
  if (isCurrentRefreshTokenExpired()) {
    console.log("refreshing session => refresh Token expired => logout"); // TODO debugging
    loginAndRedirectToSearchPage(keycloak);
  } else {
    console.log("refreshing session => refresh Token still valid => updating tokens"); // TODO debugging
    updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak);
  }
}
