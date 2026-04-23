export type LeagueName = "SCRIBE" | "STYLIST" | "AUTHOR" | "NOVELIST" | "LAUREATE" | "LUMINARY";

export const LEAGUES: { name: LeagueName; minRating: number }[] = [
  {name: "SCRIBE", minRating: 0},
  {name: "STYLIST", minRating: 30},
  {name: "AUTHOR", minRating: 60},
  {name: "NOVELIST", minRating: 90},
  {name: "LAUREATE", minRating: 120},
  {name: "LUMINARY", minRating: 150},
];

export function getLeagueFromRating(rating: number): LeagueName {
  const sortedLeagues = [...LEAGUES].sort((a, b) => b.minRating - a.minRating);
  for (const league of sortedLeagues) {
    if (rating >= league.minRating) {
      return league.name;
    }
  }
  return "SCRIBE";
}
