import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { Appointment, PaymentMethod } from '../../../core/models/appointment.model';

const PAYMENT_KEYS: Record<PaymentMethod, string> = {
  Free: 'FREE',
  Physical: 'PHYSICAL',
  Online: 'ONLINE',
  PhysicalOrOnline: 'PHYSICAL_OR_ONLINE',
};

@Component({
  selector: 'app-appointment-details',
  imports: [TranslocoModule],
  templateUrl: './appointment-details.component.html',
  styleUrl: './appointment-details.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closed.emit()' },
})
export class AppointmentDetailsComponent {
  private readonly transloco = inject(TranslocoService);
  private readonly lang = toSignal(this.transloco.langChanges$, {
    initialValue: this.transloco.getActiveLang(),
  });

  readonly appointment = input.required<Appointment>();
  readonly locationName = input<string>('');

  readonly closed = output<void>();
  readonly edit = output<string>();
  readonly cancelConfirmed = output<string>();

  readonly canModify = computed(() => this.appointment().status !== 'Cancelled');

  readonly dateLabel = computed(() => {
    const text = new Date(`${this.appointment().date}T00:00:00`).toLocaleDateString(this.lang(), {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
    return text.charAt(0).toUpperCase() + text.slice(1);
  });

  readonly timeLabel = computed(() => `${this.appointment().startTime}–${this.appointment().endTime}`);

  readonly statusKey = computed(
    () => `APPOINTMENTS.CALENDAR.STATUS.${this.appointment().status.toUpperCase()}`,
  );

  readonly paymentMethodKey = computed(
    () => `APPOINTMENTS.DETAILS.PAYMENT.METHOD.${PAYMENT_KEYS[this.appointment().paymentMethod]}`,
  );

  readonly paymentStatusKey = computed(
    () => `APPOINTMENTS.DETAILS.PAYMENT.${this.appointment().isPaid ? 'PAID' : 'UNPAID'}`,
  );
}