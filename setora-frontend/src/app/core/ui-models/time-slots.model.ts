export type TimeSlot = 'any' | 'morning' | 'afternoon' | 'evening';

// minute de la miezul nopții
export const TIME_SLOTS: Record<TimeSlot, [number | null, number | null]> = {
    any: [null, null],
    morning: [360, 720],     // 06:00–12:00
    afternoon: [720, 1080],  // 12:00–18:00
    evening: [1080, 1380],   // 18:00–23:00
};

export const TIME_SLOT_KEYS = Object.keys(TIME_SLOTS) as TimeSlot[];

export function slotOf(timeFrom: number | null, timeTo: number | null): TimeSlot {
    return TIME_SLOT_KEYS.find((s) => TIME_SLOTS[s][0] === timeFrom && TIME_SLOTS[s][1] === timeTo) ?? 'any';
}