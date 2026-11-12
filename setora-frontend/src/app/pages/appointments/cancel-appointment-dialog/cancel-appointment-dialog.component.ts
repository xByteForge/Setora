import { ChangeDetectionStrategy, Component, computed, HostListener, inject, input, output } from '@angular/core';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { Appointment } from '../../../core/models/appointment.model';

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
    readonly busy = input(false);
    readonly failed = input(false);

    readonly closed = output<void>();
    readonly confirmed = output<void>();

    readonly when = computed(() => {
        const a = this.appointment();
        const locale = this.transloco.getActiveLang() === 'ro' ? 'ro-RO' : 'en-GB';
        const date = new Intl.DateTimeFormat(locale, {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
        }).format(new Date(`${a.date}T00:00:00`));
        return `${date} · ${a.startTime}–${a.endTime}`;
    });

    close(): void {
        if (!this.busy()) this.closed.emit();
    }

    @HostListener('document:keydown.escape')
    onEscape(): void {
        this.close();
    }
}