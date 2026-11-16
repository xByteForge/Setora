import { DayOfWeek } from "./enums/day-of-week.model";
import { UnavailableType } from "./enums/unavaliable-type.model";

export interface UnavailableSlot {
  id: number;
  employeeId: number;
  type: UnavailableType;
  reason: string | null;
  specificDate: string | null; 
  dayOfWeek: DayOfWeek | null; // null for one-off slots
  startTime: string;           
  endTime: string;
}