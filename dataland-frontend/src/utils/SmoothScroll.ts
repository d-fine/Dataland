/**
 * Animation function
 *
 * @param currentTime - Current time in milliseconds, automatically passed by requestAnimationFrame
 * @param startTime - Start time of the animation, which is used to calculate the animation duration
 * @param startPosition - Starting position of the animation
 * @param distance - Distance to be scrolled during the animation
 * @param duration - Duration of the animation
 */
function animation(
  currentTime: number,
  startTime: number,
  startPosition: number,
  distance: number,
  duration: number
): void {
  if (!startTime) {
    startTime = currentTime;
  }
  const elapsedTime = currentTime - startTime;
  const scrollPosition = distance * (elapsedTime / duration) + startPosition;
  window.scrollTo(0, scrollPosition);
  if (elapsedTime < duration)
    requestAnimationFrame((currentTime) => animation(currentTime, startTime, startPosition, distance, duration));
}

/**
 * Smooth scrolling
 *
 * @param target - target element
 */
export function smoothScroll(target: string): void {
  const targetElement = document.querySelector(target) as HTMLElement;
  const targetPosition = targetElement.getBoundingClientRect().top + window.scrollY - 100;
  const startPosition = window.scrollY;
  const distance = targetPosition - startPosition;
  const duration = 300 + Math.abs(distance) / 10;
  const startTime = null as unknown as number;
  requestAnimationFrame((currentTime) => animation(currentTime, startTime, startPosition, distance, duration));
}
