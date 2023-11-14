import { defineStore } from "pinia";
import { TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS } from "@/utils/Constants";

export const useSharedSessionStateStore = defineStore("sharedSessionStateStore", {
  state: () => {
    return {
      refreshToken: undefined as undefined | string,
      refreshTokenExpiryTimestampInMs: undefined as undefined | number,
    };
  },
  getters: {
    sessionWarningTimestampInMs: (state) => {
      if (state.refreshTokenExpiryTimestampInMs) {
        return (
          state.refreshTokenExpiryTimestampInMs - TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS
        );
      }
    },
  },

  /*
  share: {
    enable: true,
  },TODO compiler says this is not needed.  Are we sure? Lets investigate*/
});
