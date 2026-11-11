import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';

const toMinutes = (iso: string): number => {
    const d = new Date(iso);
    return d.getHours() * 60 + d.getMinutes();
};

const toTime = (iso: string): string => {
    const d = new Date(iso);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

@Component({
    selector: 'app-calendar-appointment-block',
    imports: [TranslocoModule],
    templateUrl: './calendar-appointment-block.component.html',
    styleUrl: './calendar-appointment-block.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[class]': 'appointment().status',
        '[class.compact]': 'compact()',
        '[attr.title]': 'tooltip()',
        '[style.top.px]': 'top()',
        '[style.height.px]': 'height()',
        '[style.left]': 'left()',
        '[style.width]': 'width()',
    },
})
export class CalendarAppointmentBlockComponent {
    readonly appointment = input.required<CalendarAppointment>();
    readonly startHour = input.required<number>();
    readonly pxPerMinute = input.required<number>();

    readonly top = computed(
        () => (toMinutes(this.appointment().start) - this.startHour() * 60) * this.pxPerMinute(),
    );

    readonly height = computed(
        () => (toMinutes(this.appointment().end) - toMinutes(this.appointment().start)) * this.pxPerMinute(),
    );

    readonly timeRange = computed(
        () => `${toTime(this.appointment().start)}–${toTime(this.appointment().end)}`,
    );

    readonly lane = input(0);
    readonly lanes = input(1);

    readonly left = computed(() => `calc(6px + (100% - 12px) * ${this.lane()} / ${this.lanes()})`);
    readonly width = computed(() => `calc((100% - 12px) / ${this.lanes()})`);

    readonly compact = input(false);

    readonly tooltip = computed(
        () => `${this.timeRange()} · ${this.appointment().clientName} · ${this.appointment().service}`,
    );
}