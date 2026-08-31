const defaultLadder = 'LT5:0,LT4:100,LT3:200,LT2:300,LT1:400,HT5:600,HT4:800,HT3:1000,HT2:1200,HT1:1500';

let cached = null;

/**
 * The rank ladder, lowest requirement first.
 *
 * The plugin keeps the real ladder in `pvp.yml`. The bot cannot read that file reliably from
 * wherever it happens to run, so it takes the same list through `PVP_RANKS` instead. Keep the two
 * in step: an edit in `pvp.yml` that is not mirrored here only changes what Discord prints, never
 * what the server actually awards.
 */
export function getRankLadder() {
  if (cached) {
    return cached;
  }

  const raw = String(process.env.PVP_RANKS || defaultLadder).trim() || defaultLadder;
  const ladder = raw
    .split(',')
    .map(entry => entry.trim())
    .filter(Boolean)
    .map(entry => {
      const separator = entry.lastIndexOf(':');
      if (separator < 0) {
        return null;
      }
      const id = entry.slice(0, separator).trim();
      const elo = Number(entry.slice(separator + 1).trim());
      if (!id || !Number.isFinite(elo)) {
        return null;
      }
      return { id, elo };
    })
    .filter(Boolean)
    .sort((a, b) => a.elo - b.elo);

  cached = ladder.length > 0 ? ladder : parseLadder(defaultLadder);
  return cached;
}

function parseLadder(raw) {
  return raw.split(',').map(entry => {
    const [id, elo] = entry.split(':');
    return { id: id.trim(), elo: Number(elo) };
  });
}

/**
 * The rank an Elo score holds: the highest one it still meets.
 *
 * Below the cheapest rank the player keeps that cheapest rank rather than having none, which is
 * the same floor the plugin applies.
 */
export function resolveRank(elo) {
  const ladder = getRankLadder();
  if (ladder.length === 0) {
    return null;
  }

  let current = ladder[0];
  for (const rank of ladder) {
    if (Number(elo) >= rank.elo) {
      current = rank;
    }
  }
  return current;
}

export function rankName(elo) {
  const rank = resolveRank(elo);
  return rank ? rank.id : '-';
}
