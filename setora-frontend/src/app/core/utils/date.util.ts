/**
 * Fusul orar al businessului. Backendul nu il trimite, deci il tinem aici.
 * Toate conversiile din UI folosesc aceasta constanta, indiferent de fusul browserului.
 */
export const BUSINESS_TIMEZONE = 'Europe/Bucharest';

const formatters = new Map<string, Intl.DateTimeFormat>();

const formatterFor = (tz: string): Intl.DateTimeFormat => {
  let formatter = formatters.get(tz);
  if (!formatter) {
    formatter = new Intl.DateTimeFormat('en-US', {
      timeZone: tz,
      hourCycle: 'h23',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
    formatters.set(tz, formatter);
  }
  return formatter;
};

/** Componentele ceasului din fusul dat, pentru un moment (ms). */
const wallParts = (ms: number, tz: string): Record<string, number> => {
  const parts: Record<string, number> = {};
  if (isNaN(ms)) return parts;
  for (const part of formatterFor(tz).formatToParts(new Date(ms))) {
    if (part.type !== 'literal') parts[part.type] = Number(part.value);
  }
  return parts;
};

/** Ceasul din fusul dat, exprimat ca UTC "fals". Folosit doar pentru calculul offsetului. */
const wallClockMs = (ms: number, tz: string): number => {
  const p = wallParts(ms, tz);
  if (p['year'] === undefined) return ms;
  return Date.UTC(p['year'], p['month'] - 1, p['day'], p['hour'], p['minute'], p['second']);
};

const offsetMs = (ms: number, tz: string): number => wallClockMs(ms, tz) - ms;

const pad = (n: number): string => String(n).padStart(2, '0');

/** Ca toISOString, dar fara milisecunde, ca Instant-ul din Java (ex. 2026-11-07T07:30:00Z). */
const toIso = (ms: number): string => new Date(ms).toISOString().replace('.000Z', 'Z');

/** Data locala (YYYY-MM-DD) dintr-un Date din browser. Folosita pentru selectorul de zi. */
export const toIsoDate = (d: Date): string =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

/** Data (YYYY-MM-DD) in fusul businessului, pentru un Instant. */
export const businessDate = (
  instant: string | null | undefined,
  tz: string = BUSINESS_TIMEZONE,
): string => {
  if (!instant) return '';
  const ms = Date.parse(instant);
  if (isNaN(ms)) return '';
  const p = wallParts(ms, tz);
  if (!p['year']) return '';
  return `${p['year']}-${pad(p['month'])}-${pad(p['day'])}`;
};

/** Ora (HH:mm) in fusul businessului, pentru un Instant. */
export const businessTime = (
  instant: string | null | undefined,
  tz: string = BUSINESS_TIMEZONE,
): string => {
  if (!instant) return '';
  const ms = Date.parse(instant);
  if (isNaN(ms)) return '';
  const p = wallParts(ms, tz);
  if (p['hour'] === undefined) return '';
  return `${pad(p['hour'])}:${pad(p['minute'])}`;
};

/** Minutele de la miezul noptii, in fusul businessului. 00:00 inseamna 0. */
export const businessMinutesOfDay = (
  instant: string | null | undefined,
  tz: string = BUSINESS_TIMEZONE,
): number => {
  if (!instant) return 0;
  const ms = Date.parse(instant);
  if (isNaN(ms)) return 0;
  const p = wallParts(ms, tz);
  if (p['hour'] === undefined) return 0;
  return p['hour'] * 60 + p['minute'];
};

/** Data de azi (YYYY-MM-DD) in fusul businessului. */
export const todayIn = (tz: string = BUSINESS_TIMEZONE): string =>
  businessDate(new Date().toISOString(), tz);

/**
 * Transforma data si ora locale ale businessului (YYYY-MM-DD, HH:mm)
 * in Instant ISO UTC, ca sa il trimitem sau sa il comparam cu backendul.
 */
export const businessLocalToInstant = (
  date: string,
  time: string,
  tz: string = BUSINESS_TIMEZONE,
): string => {
  const [y, m, d] = date.split('-').map(Number);
  const [hh, mm] = time.split(':').map(Number);
  const wall = Date.UTC(y, m - 1, d, hh, mm);
  // Doi pasi: offsetul se poate schimba in jurul trecerii la ora de vara
  const guess = wall - offsetMs(wall, tz);
  const exact = wall - offsetMs(guess, tz);
  return toIso(exact);
};

/** Adauga minute la un Instant. */
export const addMinutes = (instant: string, minutes: number): string =>
  toIso(Date.parse(instant) + minutes * 60_000);

/** Durata in minute intre doua Instant-uri. */
export const minutesBetween = (from: string, to: string): number =>
  Math.round((Date.parse(to) - Date.parse(from)) / 60_000);
