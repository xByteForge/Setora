import { AppointmentStatus } from '../models/enums/appointment-status.model';

export interface FilterEmployee {
  id: number;
  name: string;
}

export interface TimeRange {
  /** minute de la miezul noptii, ex. 540 = 09:00 */
  from: number;
  to: number;
}

export interface AppointmentFilters {
  employeeId: number | null;        // null = toti angajatii
  status: AppointmentStatus | null; // null = toate starile
  timeRange: TimeRange;
}

export const DEFAULT_TIME_RANGE: TimeRange = { from: 9 * 60, to: 17 * 60 };

export const DEFAULT_FILTERS: AppointmentFilters = {
  employeeId: null,
  status: null,
  timeRange: DEFAULT_TIME_RANGE,
};

export const TIME_PRESETS: readonly TimeRange[] = [
  DEFAULT_TIME_RANGE,
  { from: 8 * 60, to: 12 * 60 },
  { from: 12 * 60, to: 20 * 60 },
  { from: 0, to: 24 * 60 },
];

export const STATUS_I18N_KEYS: Record<AppointmentStatus, string> = {
  CONFIRMED: 'APPOINTMENTS.SUMMARY.CONFIRMED',
  CANCELLED: 'APPOINTMENTS.SUMMARY.CANCELLED',
  UNAVAILABLE: 'APPOINTMENTS.FILTERS.STATUS_UNAVAILABLE',
};

export const minutesToLabel = (m: number): string =>
  `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;

export const rangeToLabel = (r: TimeRange): string =>
  `${minutesToLabel(r.from)}–${minutesToLabel(r.to)}`;

export const sameRange = (a: TimeRange, b: TimeRange): boolean =>
  a.from === b.from && a.to === b.to;