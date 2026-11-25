import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';

export type RegistrationStatus = 'pending' | 'success';

export interface RegisteredBusinessSummary {
  name: string;
  slug: string;
  address: string; // ex: "Bd. Dacia 34, Bucuresti, Romania"
  coverUrl: string | null;
}

@Component({
  selector: 'app-business-registration-status',
  standalone: true,
  imports: [TranslocoModule, RouterLink],
  templateUrl: './business-registration-status.component.html',
  styleUrl: './business-registration-status.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BusinessRegistrationStatusComponent {
  readonly status = input.required<RegistrationStatus>();
  readonly business = input.required<RegisteredBusinessSummary>();

  // TODO: cand exista pagina publica a businessului (business/:slug), VIEW_PROFILE trebuie sa duca acolo
  readonly discoverRoute = '/discover';
}
