/**
 * calculates the merit cost for entering ranked
 *
 * @param {number} winStreak win streak
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
 * merit earned from a submission score using a tiered base system
 * this rewards high quality consistently while providing effort bonuses for word count ;)
 *
 * @param {number} score 0-100
 * @param {number} wordCount number of words
 * @param {string} gamemode gamemode string
 * @param {boolean} isRanked whether it was a ranked match
 * @return {number} total
 */
export function calculateMerit(
  score: number,
  wordCount: number,
  gamemode: string,
  isRanked: boolean
): number {
  let baseReward = 0;

  // tiers
  if (score >= 95) baseReward = 500;
  else if (score >= 90) baseReward = 400;
  else if (score >= 80) baseReward = 300;
  else if (score >= 70) baseReward = 200;
  else if (score >= 60) baseReward = 125;
  else if (score >= 40) baseReward = 60;
  else baseReward = 25;

  // effort Bonus: rewards longer compositions without making them the primary factor
  // ~1 merit per 10 words, capped to prevent fluffing
  const effortBonus = Math.min(Math.floor(wordCount / 10), 40);

  let finalMerit = baseReward + effortBonus;

  // ranked multiplier
  if (isRanked) {
    finalMerit = Math.floor(finalMerit * 1.5);
  }

  return finalMerit;
}
