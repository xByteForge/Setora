import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';

@Component({
    selector: 'app-my-businesses',
    standalone: true,
    imports: [RouterLink, TranslocoModule],
    templateUrl: './my-businesses.component.html',
    styleUrl: './my-businesses.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyBusinessesComponent {}