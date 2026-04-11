import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";
import {
  R8_USER_ID,
  R8_USER_NAME,
  STANDARD_SYSTEM_TOURNAMENT_CONFIG,
  ON_TOPIC_SYSTEM_TOURNAMENT_CONFIG,
  SYSTEM_TOURNAMENT_CHECK_STATUSES,
  SYSTEM_TOURNAMENT_ENROLLMENT_DURATION_MS,
  SYSTEM_TOURNAMENT_SUBMISSION_DURATION_MS,
  SYSTEM_PRIZE_POOL_STEP,
} from "./systemTournamentConfig";
import {calculateTournamentProjection} from "./systemTournamentEconomy";
import {getRandomPrizePool} from "./systemTournamentPrizePool";

type SystemTournamentConfig = {
  gamemode: string;
  titlePrefix: string;
  minPrizePool: number;
  maxPrizePool: number;
  maxPlayers: number;
  minPlayers: number;
  requirements: {
    minRating: number | null;
    maxRating: number | null;
    minReputation: number | null;
    minMerit: number | null;
  };
};

async function countOpenSystemTournamentsByGamemode(
  gamemode: string
): Promise<number> {
  const snapshot = await db
    .collection("tournaments")
    .where("isSystemHosted", "==", true)
    .where("gamemode", "==", gamemode)
    .where("status", "in", SYSTEM_TOURNAMENT_CHECK_STATUSES)
    .get();

  return snapshot.size;
}

async function getNextSystemTournamentNumber(
  gamemode: string
): Promise<number> {
  const snapshot = await db
    .collection("tournaments")
    .where("isSystemHosted", "==", true)
    .where("gamemode", "==", gamemode)
    .get();

  return snapshot.size + 1;
}

async function createSystemTournament(
  config: SystemTournamentConfig
): Promise<void> {
  const now = Date.now();

  const enrollmentDeadline = now + SYSTEM_TOURNAMENT_ENROLLMENT_DURATION_MS;
  const submissionDeadline =
    enrollmentDeadline + SYSTEM_TOURNAMENT_SUBMISSION_DURATION_MS;

  const prizePool = getRandomPrizePool({
    minPrizePool: config.minPrizePool,
    maxPrizePool: config.maxPrizePool,
    step: SYSTEM_PRIZE_POOL_STEP,
  });

  const projection = calculateTournamentProjection({
    prizePool,
    maxPlayers: config.maxPlayers,
  });

  const tournamentNumber = await getNextSystemTournamentNumber(config.gamemode);

  const tournamentRef = db.collection("tournaments").doc();

  await tournamentRef.set({
    id: tournamentRef.id,
    title: `${config.titlePrefix} #${tournamentNumber}`,
    creatorId: R8_USER_ID,
    creatorName: R8_USER_NAME,
    prizePool,
    maxPlayers: config.maxPlayers,
    minPlayers: config.minPlayers,
    playersCount: 0,
    submissionsCount: 0,
    entranceFee: projection.entranceFee,
    systemFee: projection.systemFee,
    enrollmentDeadline,
    submissionDeadline,
    refunded: false,
    requirements: config.requirements,
    status: "ENROLLING",
    strictnessMultiplier: 0.92,
    createdAt: now,
    gamemode: config.gamemode,
    requiredWords: [],
    themeId: null,
    themeName: null,
    topicId: null,
    topicName: null,
    isSystemHosted: true,
  });
}

export const systemTournamentSeeder = onSchedule(
  {
    schedule: "every 24 hours",
    region: "us-central1",
  },
  async () => {
    const standardCount = await
    countOpenSystemTournamentsByGamemode("STANDARD");
    const onTopicCount = await
    countOpenSystemTournamentsByGamemode("ON_TOPIC");

    if (standardCount === 0) {
      await createSystemTournament(STANDARD_SYSTEM_TOURNAMENT_CONFIG);
    }

    if (onTopicCount === 0) {
      await createSystemTournament(ON_TOPIC_SYSTEM_TOURNAMENT_CONFIG);
    }
  }
);
