import {QueryClient} from '@tanstack/vue-query';

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
            refetchOnReconnect: true,
            refetchOnMount: true,
            staleTime: 60 * 1000, // 1 minute
        },
    },
});