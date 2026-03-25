export function adjustReputation(current: number, delta: number): number {
  const MAX = 1000;
  const MIN = -1000;

  const raw = current + delta;

  let clamped = raw;
  if (raw > MAX) clamped = MAX;
  if (raw < MIN) clamped = MIN;

  if (current !== 0 && clamped === 0) {
    return delta > 0 ? 1 : -1;
  }

  return clamped;
}

export function onRankedCompleted(current: number): number {
  return adjustReputation(current, 3);
}

export function onRankedAbandoned(current: number): number {
  return adjustReputation(current, -12);
}
