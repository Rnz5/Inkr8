/**
 * calculates merit cost for entering a ranked match.
 *
 * @param {number} winStreak  win streak
 * @param {number} lossStreak loss streak
 * @param {number} reputation reputation
 * @return {number} merit cost
 */
export function calculateRankedEntryCost(
  winStreak: number,
  lossStreak: number,
  reputation: number
): number {
  const BASE_COST = 100;
  let modifier = 1.0;

  if (winStreak > 0) {
    modifier += Math.min(winStreak * 0.05, 0.75);
  }
  if (lossStreak > 0) {
    modifier -= Math.min(lossStreak * 0.05, 0.4);
  }

  let repModifier = 1.0;

  if (reputation >= 900) repModifier = 0.80;
  else if (reputation >= 700) repModifier = 0.88;
  else if (reputation >= 400) repModifier = 0.94;
  else if (reputation >= 200) repModifier = 0.97;
  else if (reputation <= -900) repModifier = 1.40;
  else if (reputation <= -700) repModifier = 1.30;
  else if (reputation <= -400) repModifier = 1.20;
  else if (reputation <= -200) repModifier = 1.12;

  modifier *= repModifier;

  return Math.max(Math.floor(BASE_COST * modifier), 1);
}

/**
 * this calculates merit earned from a submission score
 *
 * @param {number} score 0-100%
 * @param {number} wordCount mumber of words
 * @param {string} gamemode gamemode string.
 * @param {boolean} isRanked whether it was a ranked match
 * @return {number} total merit earned
 */
export function calculateMerit(
  score: number,
  wordCount: number,
  gamemode: string,
  isRanked: boolean
): number {
  const baseRating = score / 100.0;
  let multiplier = 1.0;

  if (isRanked) {
    multiplier = 1.5;
  }

  // example
  const earnings = (wordCount * 0.5) * baseRating * multiplier;
  return Math.floor(earnings);
}
