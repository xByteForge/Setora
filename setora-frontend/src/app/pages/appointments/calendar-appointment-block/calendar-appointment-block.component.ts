import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { CalendarAppointment } from '../../../core/ui-models/calendar-appointment.model';
import { businessMinutesOfDay, businessTime, minutesBetween } from '../../../core/utils/date.util';

@Component({
    selector: 'app-calendar-appointment-block',
    imports: [TranslocoModule],
    templateUrl: './calendar-appointment-block.component.html',
    styleUrl: './calendar-appointment-block.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[class]': 'statusClass()',
        '[class.compact]': 'compact()',
        '[class.selected]': 'selected()',
        '[attr.title]': 'tooltip()',
        '[attr.role]': "'button'",
        '[attr.tabindex]': "appointment().status === 'UNAVAILABLE' ? -1 : 0",
        '[attr.aria-pressed]': 'selected()',
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
    readonly timezone = input.required<string>();
    readonly lane = input(0);
    readonly lanes = input(1);
    readonly compact = input(false);
    readonly selected = input(false);

    /** Clasa de ton vine din status in minuscule: confirmed, cancelled, unavailable. */
    readonly statusClass = computed(() => this.appointment().status.toLowerCase());

    readonly top = computed(
        () =>
            (businessMinutesOfDay(this.appointment().startsAt, this.timezone()) - this.startHour() * 60) *
            this.pxPerMinute(),
    );

    readonly height = computed(
        () => minutesBetween(this.appointment().startsAt, this.appointment().endsAt) * this.pxPerMinute(),
    );

    readonly timeRange = computed(() => {
        const a = this.appointment();
        const tz = this.timezone();
        return `${businessTime(a.startsAt, tz)}–${businessTime(a.endsAt, tz)}`;
    });

    readonly left = computed(() => `calc(6px + (100% - 12px) * ${this.lane()} / ${this.lanes()})`);
    readonly width = computed(() => `calc((100% - 12px) / ${this.lanes()})`);

    readonly tooltip = computed(
        () => `${this.timeRange()} · ${this.appointment().clientName} · ${this.appointment().serviceName}`,
    );
}