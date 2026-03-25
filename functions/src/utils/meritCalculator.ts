export function calculateMerit(
  finalScore: number,
  wordCount: number,
  gamemode: string,
  isRanked: boolean
): number {
  let baseMerit = 0;

  if (finalScore >= 90) baseMerit = 400;
  else if (finalScore >= 80) baseMerit = 300;
  else if (finalScore >= 70) baseMerit = 220;
  else if (finalScore >= 60) baseMerit = 150;
  else if (finalScore >= 50) baseMerit = 80;
  else if (finalScore >= 30) baseMerit = 40;
  else if (finalScore >= 10) baseMerit = 20;
  else if (finalScore >= 5) baseMerit = 10;

  const wordBonusMerit = Math.floor(wordCount / 30) * 20;

  const gamemodeMultiplier =
    gamemode === "ON_TOPIC" ? 1.3 : 1.1;

  const modeMultiplier = isRanked ? 1.0 : 0.4;

  return Math.floor(
    (baseMerit + wordBonusMerit) *
    gamemodeMultiplier *
    modeMultiplier
  );
}
