import { createMatchResultEmbed } from './pvp-embeds.js';

const stateKey = 'last_match_id';
const maxPerPoll = 10;

/**
 * Posts every finished ranked match into a channel.
 *
 * The plugin writes one `pvp_matches` row when a match ends, so the bot polls for rows past the
 * last one it posted rather than needing the server to call out to it. The high-water mark lives
 * in the bot's own database, which means a restart resumes where it stopped instead of either
 * replaying the whole history or losing the matches that happened while it was down.
 */
export function createMatchWatcher({ client, repository, links }) {
  const channelId = String(process.env.PVP_MATCH_CHANNEL_ID || '').trim();
  const intervalSeconds = Math.max(5, Number(process.env.PVP_MATCH_POLL_SECONDS || 15));
  let timer = null;
  let running = false;

  async function seed() {
    if (links.getState(stateKey) !== null) {
      return;
    }
    // First run posts nothing: a server that has been up for months would otherwise dump its
    // entire match history into the channel the moment the bot is switched on.
    const latest = await repository.getLatestMatchId();
    links.setState(stateKey, String(latest));
    console.log(`Match watcher starting from match #${latest}.`);
  }

  async function poll() {
    if (running) {
      return;
    }
    running = true;

    try {
      const lastId = Number(links.getState(stateKey, '0'));
      const matches = await repository.getMatchesAfter(lastId, maxPerPoll);
      if (matches.length === 0) {
        return;
      }

      const channel = await client.channels.fetch(channelId).catch(() => null);
      if (!channel?.isTextBased?.()) {
        console.warn(`PVP_MATCH_CHANNEL_ID ${channelId} is not a text channel the bot can post in.`);
        // The id stays unadvanced on purpose, so fixing the channel replays what was missed.
        return;
      }

      for (const match of matches) {
        await channel.send({ embeds: [createMatchResultEmbed(match)] });
        links.setState(stateKey, String(match.id));
      }
    } catch (error) {
      console.error('Match watcher poll failed.', error);
    } finally {
      running = false;
    }
  }

  return {
    async start() {
      if (!channelId) {
        console.log('PVP_MATCH_CHANNEL_ID is not set, so finished matches will not be posted.');
        return;
      }

      await seed();
      await poll();
      timer = setInterval(poll, intervalSeconds * 1000);
      timer.unref?.();
      console.log(`Match watcher polling every ${intervalSeconds}s into channel ${channelId}.`);
    },

    stop() {
      if (timer) {
        clearInterval(timer);
        timer = null;
      }
    }
  };
}
