import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { Appointment } from '../../../core/models/appointment.model';
import { PaymentMethod } from '../../../core/models/enums/payment-method.model';
import { CURRENCY } from '../../../core/services/catalog.service';
import { businessDate, businessTime, minutesBetween } from '../../../core/utils/date.util';

const PAYMENT_KEYS: Record<PaymentMethod, string> = {
  PHYSICAL: 'PHYSICAL',
  ONLINE: 'ONLINE',
  PHYSICAL_OR_ONLINE: 'PHYSICAL_OR_ONLINE',
};

@Component({
  selector: 'app-appointment-details',
  imports: [TranslocoModule],
  templateUrl: './appointment-details.component.html',
  styleUrl: './appointment-details.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentDetailsComponent {
  private readonly transloco = inject(TranslocoService);
  private readonly lang = toSignal(this.transloco.langChanges$, {
    initialValue: this.transloco.getActiveLang(),
  });

  readonly appointment = input.required<Appointment>();
  readonly locationName = input<string>('');
  readonly timezone = input.required<string>();

  readonly closed = output<void>();
  readonly cancelRequested = output<number>();

  readonly currency = CURRENCY;

  readonly canModify = computed(() => this.appointment().status !== 'CANCELLED');

  readonly durationMinutes = computed(() =>
    minutesBetween(this.appointment().start, this.appointment().end),
  );

  readonly dateLabel = computed(() => {
    const date = businessDate(this.appointment().start, this.timezone());
    const text = new Date(`${date}T00:00:00`).toLocaleDateString(this.lang(), {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
    return text.charAt(0).toUpperCase() + text.slice(1);
  });

  readonly timeLabel = computed(() => {
    const a = this.appointment();
    const tz = this.timezone();
    return `${businessTime(a.start, tz)}–${businessTime(a.end, tz)}`;
  });

  readonly statusKey = computed(() => `APPOINTMENTS.CALENDAR.STATUS.${this.appointment().status}`);

  readonly paymentMethodKey = computed(
    () => `APPOINTMENTS.DETAILS.PAYMENT.METHOD.${PAYMENT_KEYS[this.appointment().paymentMethod]}`,
  );
}
