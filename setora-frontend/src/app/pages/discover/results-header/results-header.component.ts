import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../../core/services/business-discovery.service';

@Component({
    selector: 'app-results-header',
    standalone: true,
    imports: [TranslocoModule],
    templateUrl: './results-header.component.html',
    styleUrl: './results-header.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResultsHeaderComponent {
    readonly service = inject(BusinessDiscoveryService);
}