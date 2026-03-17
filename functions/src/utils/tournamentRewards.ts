/**
 * Calculates tournament reward percentages for each leaderboard position.
 * Top 80% of participants receive rewards, with first place receiving 45%
 * and the remainder distributed by a decreasing power curve.
 *
 * @param {number} players - Total number of participants in the tournament.
 * @return {number[]} A list of reward percentages by placement.
 */
export function calculateRewardPercentages(players: number): number[] {
  if (players === 1) {
    return [1.0];
  }

  if (players <= 0) return [];

  const losersCount = Math.floor(players * 0.2);
  const winnersCount = players - losersCount;

  if (winnersCount <= 0) return Array(players).fill(0.0);

  const result = Array(players).fill(0.0);

  const top1Percent = 0.45;
  result[0] = top1Percent;

  if (winnersCount === 1) return result;

  const exponent = 1.4;
  const weights: number[] = [];

  for (let i = 2; i <= winnersCount; i++) {
    const weight = 1.0 / Math.pow(i, exponent);
    weights.push(weight);
  }

  const totalWeight = weights.reduce((sum, value) => sum + value, 0);
  const remainingPercent = 1.0 - top1Percent;

  for (let i = 0; i < weights.length; i++) {
    const normalized = (weights[i] / totalWeight) * remainingPercent;
    result[i + 1] = normalized;
  }

  return result;
}
