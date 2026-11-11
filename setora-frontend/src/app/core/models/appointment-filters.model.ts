import { AppointmentStatus } from './calendar-appointment.model';

export interface TimeRange {
  /** minute de la miezul nopții, ex. 540 = 09:00 */
  from: number;
  to: number;
}

export interface AppointmentFilters {
  locationId: string | null; // null = toate locațiile
  employeeId: string | null; // null = toți angajații
  status: AppointmentStatus | null; // null = toate stările
  timeRange: TimeRange;
}

export const DEFAULT_TIME_RANGE: TimeRange = { from: 9 * 60, to: 17 * 60 };

export const DEFAULT_FILTERS: AppointmentFilters = {
  locationId: null,
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

/** Cheile de traducere existente (SUMMARY) + una nouă pentru "blocked". */
export const STATUS_I18N_KEYS: Record<AppointmentStatus, string> = {
  confirmed: 'APPOINTMENTS.SUMMARY.CONFIRMED',
  pending: 'APPOINTMENTS.SUMMARY.PENDING',
  cancelled: 'APPOINTMENTS.SUMMARY.CANCELLED',
  blocked: 'APPOINTMENTS.FILTERS.STATUS_BLOCKED',
};

export const minutesToLabel = (m: number): string =>
  `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;

export const rangeToLabel = (r: TimeRange): string =>
  `${minutesToLabel(r.from)}–${minutesToLabel(r.to)}`;

export const sameRange = (a: TimeRange, b: TimeRange): boolean =>
  a.from === b.from && a.to === b.to;