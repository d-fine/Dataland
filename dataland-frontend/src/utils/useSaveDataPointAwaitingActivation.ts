import { computed, onScopeDispose, ref, watch, type ComputedRef } from 'vue';
import { useQueryClient, type Query } from '@tanstack/vue-query';
import {
  usePostDataPointMutation,
  type PostDataPointVariables,
} from '@/api-queries/backend/data-point/usePostDataPointMutation';
import { useGetDataPointMetaInfoQuery } from '@/api-queries/backend/data-point/useGetDataPointMetaInfoQuery';
import { type DataPointMetaInformation } from '@clients/backend';
import { frameworkDataKeys } from '@/api-queries/backend/framework-data/frameworkDataKeys';

const POLL_INTERVAL_MS = 500;
const ACTIVATION_TIMEOUT_MS = 15000;

export interface SaveDataPointCallbacks {
  /** Called once the data point is active (or the wait timed out) and framework data was invalidated. */
  onDone: (result: { activated: boolean }) => void;
  onError: (error: Error) => void;
}

/**
 * Posts a data point and keeps `isPending` true until the backend has activated it, which happens asynchronously
 * after the POST returned. Afterwards all cached framework data is invalidated so that data tables reload with the
 * new value. If activation is not observed within the timeout, the invalidation happens anyway and
 * `activated: false` is reported.
 *
 * @returns the `save` function and an `isPending` flag covering both the POST and the wait for activation.
 */
export function useSaveDataPointAwaitingActivation(): {
  save: (variables: PostDataPointVariables, callbacks: SaveDataPointCallbacks) => void;
  isPending: ComputedRef<boolean>;
} {
  const queryClient = useQueryClient();
  const { mutate, isPending: isPosting } = usePostDataPointMutation();
  const awaitedDataPointId = ref<string | undefined>();
  const isWaiting = computed(() => awaitedDataPointId.value !== undefined);
  let currentCallbacks: SaveDataPointCallbacks | undefined;
  let timeoutHandle: ReturnType<typeof setTimeout> | undefined;

  const metaInfoQuery = useGetDataPointMetaInfoQuery(
    { dataPointId: awaitedDataPointId },
    {
      refetchInterval: (query: Query<DataPointMetaInformation, Error>) =>
        query.state.data?.currentlyActive ? false : POLL_INTERVAL_MS,
      retry: false,
      gcTime: 0,
    }
  );

  /**
   * Finishes the waiting phase, invalidates framework data and notifies the caller.
   * @param activated whether activation was observed
   */
  async function finish(activated: boolean): Promise<void> {
    clearTimeout(timeoutHandle);
    const callbacks = currentCallbacks;
    if (!callbacks) return;
    currentCallbacks = undefined;
    await queryClient.invalidateQueries({ queryKey: frameworkDataKeys.all });
    awaitedDataPointId.value = undefined;
    callbacks.onDone({ activated });
  }

  watch(
    () => metaInfoQuery.data.value?.currentlyActive,
    (active) => {
      if (active) void finish(true);
    }
  );

  onScopeDispose(() => clearTimeout(timeoutHandle));

  return {
    save: (variables, callbacks): void => {
      currentCallbacks = callbacks;
      mutate(variables, {
        onSuccess: (metaInfo) => {
          if (metaInfo.currentlyActive) {
            void finish(true);
            return;
          }
          awaitedDataPointId.value = metaInfo.dataPointId;
          timeoutHandle = setTimeout(() => void finish(false), ACTIVATION_TIMEOUT_MS);
        },
        onError: (error) => {
          currentCallbacks = undefined;
          callbacks.onError(error);
        },
      });
    },
    isPending: computed(() => isPosting.value || isWaiting.value),
  };
}
