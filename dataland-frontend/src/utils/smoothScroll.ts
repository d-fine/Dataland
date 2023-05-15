/**
 * Animation function
 * @param currentTime - Current time in milliseconds, automatically passed by requestAnimationFrame
 * @param startTime - Start time of the animation, which is used to calculate the animation duration
 * @param startPosition - Starting position of the animation
 * @param totalDistanceToScroll - Distance to be scrolled during the animation
 * @param totalDurationForScrolling - Duration of the animation
 */
function animation(
  currentTime: number,
  startTime: number,
  startPosition: number,
  totalDistanceToScroll: number,
  totalDurationForScrolling: number
): void {
  const elapsedTime = currentTime - startTime;
  const positionToScrollTo = totalDistanceToScroll * (elapsedTime / totalDurationForScrolling) + startPosition;
  window.scrollTo(0, positionToScrollTo);
  if (elapsedTime < totalDurationForScrolling)
    requestAnimationFrame((currentTime) =>
      animation(currentTime, startTime, startPosition, totalDistanceToScroll, totalDurationForScrolling)
    );
}

/**
 * Smooth scrolling
 * @param target - target element
 */
export function smoothScroll(target: string): void {
  const targetElement = document.querySelector(target) as HTMLElement;
  const positionOfTargetElementRelativeToViewport = targetElement.getBoundingClientRect().top;
  const startPosition = window.scrollY;
  const buffer = 100;
  const targetPosition = startPosition + positionOfTargetElementRelativeToViewport - buffer;
  const totalDistanceToScroll = targetPosition - startPosition;
  const totalDurationForScrolling = 300 + Math.abs(totalDistanceToScroll) / 10;
  requestAnimationFrame((currentTime) => {
    const startTime = currentTime;
    animation(currentTime, startTime, startPosition, totalDistanceToScroll, totalDurationForScrolling);
  });
}
