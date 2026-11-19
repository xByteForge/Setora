import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessListItem } from '../../../core/ui-models/business-list-item.model';
import { LucideMapPin } from '@lucide/angular';

@Component({
    selector: 'app-business-card',
    standalone: true,
    imports: [TranslocoModule, LucideMapPin],
    templateUrl: './business-card.component.html',
    styleUrl: './business-card.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BusinessCardComponent {
    readonly business = input.required<BusinessListItem>();
    readonly view = output<number>();
    readonly imageFailed = signal(false);

    // initialele afisate cand nu exista imagine (primele doua cuvinte din nume)
    readonly initials = computed(() =>
        this.business().name
            .split(/\s+/)
            .filter(Boolean)
            .slice(0, 2)
            .map(word => word[0].toUpperCase())
            .join('')
    );
}