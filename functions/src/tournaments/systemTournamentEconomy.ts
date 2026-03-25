import {SYSTEM_CREATION_FEE_PERCENT,
  PROFIT_MARGIN_PERCENT} from "./tournamentEconomyConfig";

export type TournamentEconomyProjection = {
  prizePool: number;
  maxPlayers: number;
  entranceFee: number;
  totalRevenue: number;
  systemFee: number;
  netProfit: number;
  breakEvenPlayers: number;
};

export function calculateTournamentProjection(params: {
  prizePool: number;
  maxPlayers: number;
}): TournamentEconomyProjection {
  const {prizePool, maxPlayers} = params;

  if (prizePool <= 0) {
    throw new Error("Prize pool must be positive");
  }

  if (maxPlayers <= 1) {
    throw new Error("At least 2 players required");
  }

  const systemFee = Math.floor(prizePool * SYSTEM_CREATION_FEE_PERCENT);

  const targetRevenue = Math.floor(prizePool * (1 + PROFIT_MARGIN_PERCENT));

  const entranceFee = Math.ceil(targetRevenue / maxPlayers);

  const totalRevenue = entranceFee * maxPlayers;

  const netProfit = totalRevenue - prizePool - systemFee;

  const breakEvenPlayers = Math.ceil((prizePool + systemFee) / entranceFee);

  return {
    prizePool,
    maxPlayers,
    entranceFee,
    totalRevenue,
    systemFee,
    netProfit,
    breakEvenPlayers,
  };
}
