import { ChangeDetectionStrategy, Component, computed, HostListener, inject, input, output } from '@angular/core';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { Appointment } from '../../../core/models/appointment.model';
import { businessDate, businessTime, minutesBetween } from '../../../core/utils/date.util';

@Component({
    selector: 'app-cancel-appointment-dialog',
    imports: [TranslocoModule],
    templateUrl: './cancel-appointment-dialog.component.html',
    styleUrl: './cancel-appointment-dialog.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CancelAppointmentDialogComponent {
    private readonly transloco = inject(TranslocoService);

    readonly appointment = input.required<Appointment>();
    readonly locationName = input('');
    readonly timezone = input.required<string>();
    readonly busy = input(false);
    readonly failed = input(false);

    readonly closed = output<void>();
    readonly confirmed = output<void>();

    readonly durationMinutes = computed(() =>
        minutesBetween(this.appointment().startsAt, this.appointment().endsAt),
    );

    readonly when = computed(() => {
        const a = this.appointment();
        const tz = this.timezone();
        const locale = this.transloco.getActiveLang() === 'ro' ? 'ro-RO' : 'en-GB';
        const date = new Intl.DateTimeFormat(locale, {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
        }).format(new Date(`${businessDate(a.startsAt, tz)}T00:00:00`));
        return `${date} · ${businessTime(a.startsAt, tz)}–${businessTime(a.endsAt, tz)}`;
    });

    close(): void {
        if (!this.busy()) this.closed.emit();
    }

    @HostListener('document:keydown.escape')
    onEscape(): void {
        this.close();
    }
}