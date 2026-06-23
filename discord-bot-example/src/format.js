export function formatValue(type, player) {
  const value = player[toCamelCase(type.column)];
  if (type.formatter === 'money') {
    return formatMoney(value);
  }
  if (type.formatter === 'time') {
    return formatPlaytime(value);
  }
  return formatNumber(value);
}

export function formatMoney(value) {
  return `$${formatCompact(value)}`;
}

export function formatNumber(value) {
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(Number(value || 0));
}

export function formatPlaytime(totalSeconds) {
  let seconds = Math.max(0, Math.floor(Number(totalSeconds || 0)));
  const days = Math.floor(seconds / 86400);
  seconds %= 86400;
  const hours = Math.floor(seconds / 3600);
  seconds %= 3600;
  const minutes = Math.floor(seconds / 60);

  const parts = [];
  if (days > 0) {
    parts.push(`${days}d`);
  }
  if (hours > 0) {
    parts.push(`${hours}h`);
  }
  if (minutes > 0 || parts.length === 0) {
    parts.push(`${minutes}m`);
  }
  return parts.join(' ');
}

export function formatKdr(kills, deaths) {
  if (Number(deaths) <= 0) {
    return Number(kills) <= 0 ? '0.00' : formatNumber(kills);
  }
  return (Number(kills) / Number(deaths)).toFixed(2);
}

function formatCompact(value) {
  const number = Number(value || 0);
  if (Math.abs(number) >= 1_000_000_000) {
    return `${trim(number / 1_000_000_000)}B`;
  }
  if (Math.abs(number) >= 1_000_000) {
    return `${trim(number / 1_000_000)}M`;
  }
  if (Math.abs(number) >= 1_000) {
    return `${trim(number / 1_000)}K`;
  }
  return trim(number);
}

function trim(value) {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 1
  }).format(value);
}

function toCamelCase(column) {
  return column.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
}
