import { SESSION_TIMEOUT_IN_MS } from "@/utils/Constants";

const keyForSessionTimeoutTimestampInLocalStorage = "sessionTimeoutTimestamp";

/**
 * TODO
 */
export function resetSessionTimeoutTimestampInLocalStorage(): void {
  const currentTimeInMs = new Date().getTime();
  const sessionTimeoutTimestampInMs = currentTimeInMs + SESSION_TIMEOUT_IN_MS;
  localStorage.setItem(keyForSessionTimeoutTimestampInLocalStorage, sessionTimeoutTimestampInMs.toString());
}

/**
 * TODO
 */
export function getSessionTimeoutTimestampFromLocalStorage(): number {
  const sessionTimeoutTimestampInLocalStorage = localStorage.getItem(keyForSessionTimeoutTimestampInLocalStorage);
  if (sessionTimeoutTimestampInLocalStorage) {
    return parseInt(sessionTimeoutTimestampInLocalStorage);
  } else {
    throw Error(`Required timestamp for the session timeout could not be found in the local storage of the browser" +
        "by using the key ${keyForSessionTimeoutTimestampInLocalStorage}.`);
  }
}
