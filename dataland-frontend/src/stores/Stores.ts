import { defineStore } from 'pinia';
import { TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS } from '@/utils/Constants';

type SharedSessionState = {
  refreshToken: string | undefined;
  refreshTokenExpiryTimestampInMs: number | undefined;
};

export const useSharedSessionStateStore = defineStore('sharedSessionStateStore', {
  state: (): SharedSessionState => {
    return {
      refreshToken: undefined as undefined | string,
      refreshTokenExpiryTimestampInMs: undefined as undefined | number,
    };
  },
  getters: {
    sessionWarningTimestampInMs: (state: SharedSessionState) => {
      if (state.refreshTokenExpiryTimestampInMs) {
        return (
          state.refreshTokenExpiryTimestampInMs - TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS
        );
      }
    },
  },
});
