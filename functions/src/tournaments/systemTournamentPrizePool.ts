export function getRandomPrizePool(params: {
  minPrizePool: number;
  maxPrizePool: number;
  step: number;
}): number {
  const {minPrizePool, maxPrizePool, step} = params;

  if (minPrizePool > maxPrizePool) {
    throw new Error("minPrizePool cannot be greater than maxPrizePool");
  }

  if (step <= 0) {
    throw new Error("step must be positive");
  }

  const numberOfSteps = Math.floor((maxPrizePool - minPrizePool) / step);
  const randomStep = Math.floor(Math.random() * (numberOfSteps + 1));

  return minPrizePool + (randomStep * step);
}
