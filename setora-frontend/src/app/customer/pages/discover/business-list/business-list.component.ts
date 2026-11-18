import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessListItem } from '../../../../core/ui-models/business-list-item.model';
import { BusinessCardComponent } from '../business-card/business-card.component';

@Component({
    selector: 'app-business-list',
    standalone: true,
    imports: [BusinessCardComponent, TranslocoModule],
    templateUrl: './business-list.component.html',
    styleUrl: './business-list.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BusinessListComponent {
    readonly items = input.required<BusinessListItem[]>();
    /** O singură coloană (când panoul de filtre e deschis). */
    readonly single = input(false);
    readonly view = output<number>();
}