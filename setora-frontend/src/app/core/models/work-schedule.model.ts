import { DayOfWeek } from "./enums/day-of-week.model";

export interface WorkSchedule {
  id: number;
  employeeId: number;
  dayOfWeek: DayOfWeek;
  startTime: string; 
  endTime: string;
}