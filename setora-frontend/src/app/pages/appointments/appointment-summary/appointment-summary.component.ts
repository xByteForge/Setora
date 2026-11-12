import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoDirective } from '@ngneat/transloco';
import {
    LucideCalendar,
    LucideCircleCheck,
    LucideClock,
    LucideCircleX,
} from '@lucide/angular';
import { AppointmentSummary } from '../../../core/models/appointment-summary.model';

@Component({
    selector: 'app-appointment-summary',
    standalone: true,
    imports: [TranslocoDirective, LucideCalendar, LucideCircleCheck, LucideClock, LucideCircleX],
    styleUrl: './appointment-summary.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
    <section
      class="summary"
      *transloco="let t"
      role="group"
      aria-live="polite"
      [attr.aria-label]="t('APPOINTMENTS.SUMMARY.TITLE')"
    >
      <article class="card total">
        <svg lucideCalendar [size]="20" aria-hidden="true"></svg>
        <span class="value">{{ summary().total }}</span>
        <span class="label">{{ t('APPOINTMENTS.SUMMARY.TODAY') }}</span>
      </article>

      <article class="card confirmed">
        <svg lucideCircleCheck [size]="20" aria-hidden="true"></svg>
        <span class="value">{{ summary().confirmed }}</span>
        <span class="label">{{ t('APPOINTMENTS.SUMMARY.CONFIRMED') }}</span>
      </article>

      <article class="card pending">
        <svg lucideClock [size]="20" aria-hidden="true"></svg>
        <span class="value">{{ summary().pending }}</span>
        <span class="label">{{ t('APPOINTMENTS.SUMMARY.PENDING') }}</span>
      </article>

      <article class="card cancelled">
        <svg lucideCircleX [size]="20" aria-hidden="true"></svg>
        <span class="value">{{ summary().cancelled }}</span>
        <span class="label">{{ t('APPOINTMENTS.SUMMARY.CANCELLED') }}</span>
      </article>
    </section>
  `,
})
export class AppointmentSummaryComponent {
    readonly summary = input.required<AppointmentSummary>();
}