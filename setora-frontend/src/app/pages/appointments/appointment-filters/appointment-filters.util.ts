import { Appointment, Employee } from '../../../core/models/calendar-appointment.model';
import { AppointmentFilters } from '../../../core/models/appointment-filters.model';

const minutesOfDay = (iso: string): number => {
  const d = new Date(iso);
  return d.getHours() * 60 + d.getMinutes();
};

/** Toate filtrele active se combină cu ȘI. `null` = fără restricție. */
export function filterAppointments(
  items: readonly Appointment[],
  f: AppointmentFilters,
): Appointment[] {
  return items.filter((a) => {
    if (f.locationId && a.locationId !== f.locationId) return false;
    if (f.employeeId && a.employeeId !== f.employeeId) return false;
    if (f.status && a.status !== f.status) return false;

    // Rămân și programările care se suprapun doar parțial cu intervalul ales.
    return minutesOfDay(a.start) < f.timeRange.to && minutesOfDay(a.end) > f.timeRange.from;
  });
}

/** Coloanele de angajați afișate în calendar. */
export function filterEmployees(
  all: readonly Employee[],
  f: AppointmentFilters,
): Employee[] {
  return all.filter(
    (e) =>
      (!f.locationId || e.locationIds.includes(f.locationId)) &&
      (!f.employeeId || e.id === f.employeeId),
  );
}

/** Orele de pe axa verticală a calendarului (9, 10, ...). */
export function visibleHours(f: AppointmentFilters): number[] {
  const first = Math.floor(f.timeRange.from / 60);
  const last = Math.ceil(f.timeRange.to / 60);
  return Array.from({ length: Math.max(last - first, 0) }, (_, i) => first + i);
}