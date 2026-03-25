export function calculateNewRating(
  currentRating: number,
  score: number,
  kFactor = 32
): number {
  const expectedScore = 60.0;
  const delta = (score - expectedScore) / 100.0;

  const newRating = currentRating + (kFactor * delta);

  return Math.max(0, Math.floor(newRating));
}
