import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { loginAndRedirectToSearchPage, logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import Keycloak from "keycloak-js";
import { useSharedSessionStateStore } from "@/stores/stores";

const minRequiredRemainingValidityTimeOfRefreshTokenDuringCheck = TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS + 1000;

/**
 * Updates the token, parses its expiry timestamp, and stores both of these values in the shared pina store.
 *
 * @param keycloak is the keycloak adaptor used to actually update the token
 * @param forceStoringValues forces storing the refresh token and its expiry timestamp in the shared storage, even if
 * the updateToken() has not done an update itself because of the minValidity value
 */
export function updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak: Keycloak, forceStoringValues = false): void {
  keycloak
    .updateToken(60)
    .then((hasTokenBeenUpdated) => {
      if (hasTokenBeenUpdated || forceStoringValues) {
        const refreshTokenExpiryTime = keycloak.refreshTokenParsed?.exp;
        if (refreshTokenExpiryTime) {
          useSharedSessionStateStore().refreshToken = keycloak.refreshToken;
          useSharedSessionStateStore().refreshTokenExpiryTimestampInMs = refreshTokenExpiryTime * 1000;
        }
      }
    })
    .catch(() => {
      console.log(`Could not refresh token`);
    });
}

/**
 * Starts a setInterval-method which - in fixed time intervals - checks if the session warning timestamp was surpassed.
 * If it is surpassed, the provided callback function will be executed.
 *
 * @param keycloak is the keycloak adaptor used to do a logout in case the session warning timestamp cannot be retrieved
 * @param onSurpassingExpiredSessionTimestampCallback is the callback function which will be executed as soon as the
 * session warning timestamp is surpassed
 * @returns the function ID of the setInterval-execution as number
 */
export function startSessionSetIntervalFunctionAndReturnItsId(
  keycloak: Keycloak,
  onSurpassingExpiredSessionTimestampCallback: () => void
): number {
  const functionIdOfSetInterval = setInterval(() => {
    const currentTimestampInMs = new Date().getTime();
    const sessionWarningTimestamp = useSharedSessionStateStore().sessionWarningTimestampInMs;
    if (!sessionWarningTimestamp) {
      logoutAndRedirectToUri(keycloak, "");
    } else {
      if (currentTimestampInMs >= sessionWarningTimestamp) {
        clearInterval(functionIdOfSetInterval);
        onSurpassingExpiredSessionTimestampCallback();
      }
    }
  }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
  return functionIdOfSetInterval;
}

/**
 * Checks if the expiry timestamp for the refresh token, which is stored in the shared pinia store, has already
 * been reached in the moment of the function execution.
 *
 * @returns a boolean to express if the timestamp has already been reached or not
 */
export function isRefreshTokenExpiryTimestampInSharedStoreReached(): boolean {
  const currentTimestamp = new Date().getTime();
  const refreshTokenExpiryTimestampInMs = useSharedSessionStateStore().refreshTokenExpiryTimestampInMs;
  if (refreshTokenExpiryTimestampInMs) {
    return (
      currentTimestamp + minRequiredRemainingValidityTimeOfRefreshTokenDuringCheck > refreshTokenExpiryTimestampInMs
    );
  } else {
    throw Error(
      "No expiry timestamp for the current refresh token could be found in the store. " +
        "This is not acceptable for running Dataland."
    );
  }
}

/**
 * Tries to refresh a session. If the refresh token expiry timestamp in the shared store has been reached during the
 * execution of this method, it logs out the user out to finally close the session.
 * Else it refreshes the session by updating the token.
 *
 * @param keycloak is the keycloak adaptor used to do the required actions (logout or token update)
 */
export function tryToRefreshSession(keycloak: Keycloak): void {
  /*
  Even after the session idle timeout in the Keycloak settings is reached (which means the refresh token
  is invalid too), Keycloak still keeps the session alive for 2 minutes (observed behaviour).
  During these 2 minutes of "grace time", refreshing the session leads to a re-login without entering any credentials.
  After the grace time, you will be redirected to the usual Keycloak login page.
   */
  if (isRefreshTokenExpiryTimestampInSharedStoreReached()) {
    loginAndRedirectToSearchPage(keycloak);
  } else {
    updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak);
  }
}
