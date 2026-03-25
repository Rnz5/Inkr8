export const R8_USER_ID = "R8";
export const R8_USER_NAME = "R8";

export const SYSTEM_TOURNAMENT_ENROLLMENT_DURATION_MS =
  12 * 60 * 60 * 1000;

export const SYSTEM_TOURNAMENT_SUBMISSION_DURATION_MS =
  24 * 60 * 60 * 1000;

export const SYSTEM_TOURNAMENT_CHECK_STATUSES = [
  "ENROLLING",
  "ACTIVE",
];

export const SYSTEM_PRIZE_POOL_STEP = 100;

export const STANDARD_SYSTEM_TOURNAMENT_CONFIG = {
  gamemode: "STANDARD",
  titlePrefix: "Standard Tournament Edition ",
  minPrizePool: 6000,
  maxPrizePool: 12000,
  maxPlayers: 20,
  minPlayers: 6,
  requirements: {
    minRating: null,
    maxRating: null,
    minReputation: null,
    minMerit: null,
  },
};

export const ON_TOPIC_SYSTEM_TOURNAMENT_CONFIG = {
  gamemode: "ON_TOPIC",
  titlePrefix: "On-Topic Tournament Edition ",
  minPrizePool: 8000,
  maxPrizePool: 14000,
  maxPlayers: 20,
  minPlayers: 6,
  requirements: {
    minRating: null,
    maxRating: null,
    minReputation: null,
    minMerit: null,
  },
};
