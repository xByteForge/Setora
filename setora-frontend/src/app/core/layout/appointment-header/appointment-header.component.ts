import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslocoDirective } from '@ngneat/transloco';
import { LucidePlus } from '@lucide/angular';
import { AppointmentSummary } from '../../models/appointment-summary.model';
import { AppointmentSummaryComponent } from '../appointment-summary/appointment-summary.component';

@Component({
    selector: 'app-appointments-header',
    standalone: true,
    imports: [TranslocoDirective, LucidePlus, AppointmentSummaryComponent],
    styleUrl: './appointments-header.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
    <header class="page-header" *transloco="let t">
      <div class="top">
        <div class="titles">
          <h1>{{ t('APPOINTMENTS.TITLE') }}</h1>
          <p class="subtitle">{{ t('APPOINTMENTS.SUBTITLE') }}</p>
        </div>

        <button type="button" class="btn-primary" (click)="addClicked.emit()">
          <svg lucidePlus [size]="16" aria-hidden="true"></svg>
          {{ t('APPOINTMENTS.ADD') }}
        </button>
      </div>

      <app-appointment-summary [summary]="summary()" />
    </header>
  `,
})
export class AppointmentsHeaderComponent {
    readonly summary = input.required<AppointmentSummary>();
    readonly addClicked = output<void>();
}