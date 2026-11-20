import { AppointmentStatus } from '../../../core/models/enums/appointment-status.model';
import {
  AppointmentFilters,
  FilterEmployee,
} from '../../../core/ui-models/appointment-filters.model';
import { BUSINESS_TIMEZONE, businessMinutesOfDay } from '../../../core/utils/date.util';

/** Campurile pe care se filtreaza. Appointment (si CalendarAppointment) le are pe toate. */
export interface FilterableAppointment {
  employeeId: number;
  status: AppointmentStatus;
  start: string;
  end: string;
}

/** Minutul de final in fusul businessului. 00:00 inseamna sfarsitul zilei (1440). */
const endMinutes = (instant: string, tz: string): number => {
  const m = businessMinutesOfDay(instant, tz);
  return m === 0 ? 24 * 60 : m;
};

/** Toate filtrele active se combina cu SI. `null` = fara restrictie. */
export function filterAppointments<T extends FilterableAppointment>(
  items: readonly T[],
  f: AppointmentFilters,
  tz: string = BUSINESS_TIMEZONE,
): T[] {
  return items.filter((a) => {
    if (f.employeeId !== null && a.employeeId !== f.employeeId) return false;
    if (f.status && a.status !== f.status) return false;

    const start = businessMinutesOfDay(a.start, tz);
    const end = endMinutes(a.end, tz);
    return start < f.timeRange.to && end > f.timeRange.from;
  });
}

/** Angajatii afisati ca coloane in calendar. */
export function filterEmployees<T extends { id: number }>(
  all: readonly T[],
  f: AppointmentFilters,
): T[] {
  return all.filter((e) => f.employeeId === null || e.id === f.employeeId);
}
