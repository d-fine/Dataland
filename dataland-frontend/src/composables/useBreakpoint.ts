import { ref, onMounted, onUnmounted, readonly, type Ref, type DeepReadonly } from 'vue';

export type BreakpointKey = 'sm' | 'md' | 'lg' | 'xl';

interface BreakpointReturn {
  width: DeepReadonly<Ref<number>>;
  isBelow: (bp: BreakpointKey) => boolean;
  isAbove: (bp: BreakpointKey) => boolean;
  isMobile: () => boolean;
  isTablet: () => boolean;
  isDesktop: () => boolean;
}

const BP_VALUES: Record<BreakpointKey, number> = {
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1440,
};

/**
 * Provides reactive viewport width tracking and breakpoint helper functions.
 */
export function useBreakpoint(): BreakpointReturn {
  const width = ref(globalThis.innerWidth ?? 1440);

  const onResize = (): void => {
    width.value = globalThis.innerWidth;
  };

  onMounted(() => {
    globalThis.addEventListener('resize', onResize);
    onResize();
  });

  onUnmounted(() => {
    globalThis.removeEventListener('resize', onResize);
  });

  const isBelow = (bp: BreakpointKey): boolean => width.value < BP_VALUES[bp];
  const isAbove = (bp: BreakpointKey): boolean => width.value >= BP_VALUES[bp];

  return {
    width: readonly(width),
    isBelow,
    isAbove,
    isMobile: (): boolean => isBelow('md'),
    isTablet: (): boolean => isAbove('md') && isBelow('lg'),
    isDesktop: (): boolean => isAbove('lg'),
  };
}
