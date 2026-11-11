import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';

export interface PositionedAppointment {
    appointment: CalendarAppointment;
    lane: number;  // pe ce "banda" sta (0, 1, 2...)
    lanes: number; // cate benzi are grupul de suprapuneri
}

export const layoutOverlaps = (items: CalendarAppointment[]): PositionedAppointment[] => {
    const sorted = [...items].sort(
        (a, b) => new Date(a.start).getTime() - new Date(b.start).getTime(),
    );

    const result: PositionedAppointment[] = [];
    let cluster: PositionedAppointment[] = [];
    let laneEnds: number[] = [];
    let clusterEnd = 0;

    const flush = () => {
        for (const p of cluster) p.lanes = laneEnds.length;
        result.push(...cluster);
        cluster = [];
        laneEnds = [];
    };

    for (const appointment of sorted) {
        const start = new Date(appointment.start).getTime();
        const end = new Date(appointment.end).getTime();

        if (cluster.length && start >= clusterEnd) flush();

        let lane = laneEnds.findIndex((laneEnd) => laneEnd <= start);
        if (lane === -1) {
            lane = laneEnds.length;
            laneEnds.push(end);
        } else {
            laneEnds[lane] = end;
        }

        clusterEnd = cluster.length ? Math.max(clusterEnd, end) : end;
        cluster.push({ appointment, lane, lanes: 1 });
    }

    flush();
    return result;
};