/**
 * Smooth scroling
 *
 * @param target - target element
 */
export function smoothScroll(target: string): void {
  const targetElement = document.querySelector(target) as HTMLElement;
  const targetPosition = targetElement.getBoundingClientRect().top + window.scrollY - 100;
  const startPosition = window.scrollY;
  const distance = targetPosition - startPosition;
  const duration = 300;
  let startTime = null as unknown as number;

  const animation = (currentTime: number): void => {
    if (!startTime) {
      startTime = currentTime;
    }
    const elapsedTime = currentTime - startTime;
    const scrollPosition = distance * (elapsedTime / duration) + startPosition;
    window.scrollTo(0, scrollPosition);
    if (elapsedTime < duration) requestAnimationFrame(animation);
  };
  requestAnimationFrame(animation);
}
