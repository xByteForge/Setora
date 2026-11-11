import { AppointmentFilters, FilterEmployee } from '../../../core/models/appointment-filters.model';

/** Câmpurile pe care se filtrează. CalendarAppointment trebuie să le aibă. */
export interface FilterableAppointment {
  employeeId: string;
  locationId?: string;
  status: string;
  start: string | Date;
  end: string | Date;
}

const minutesOfDay = (value: string | Date): number => {
  const d = new Date(value);
  return d.getHours() * 60 + d.getMinutes();
};

/** Toate filtrele active se combină cu ȘI. `null` = fără restricție. */
export function filterAppointments<T extends FilterableAppointment>(
  items: readonly T[],
  f: AppointmentFilters,
): T[] {
  return items.filter((a) => {
    if (f.locationId && a.locationId !== f.locationId) return false;
    if (f.employeeId && a.employeeId !== f.employeeId) return false;
    if (f.status && a.status !== f.status) return false;

    // Rămân și programările care se suprapun doar parțial cu intervalul ales.
    return minutesOfDay(a.start) < f.timeRange.to && minutesOfDay(a.end) > f.timeRange.from;
  });
}

/** Angajații afișați ca coloane în calendar. */
export function filterEmployees<T extends Pick<FilterEmployee, 'id' | 'locationIds'>>(
  all: readonly T[],
  f: AppointmentFilters,
): T[] {
  return all.filter(
    (e) =>
      (!f.locationId || (e.locationIds?.includes(f.locationId) ?? true)) &&
      (!f.employeeId || e.id === f.employeeId),
  );
}

export const isSameDay = (a: Date, b: Date): boolean =>
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate();