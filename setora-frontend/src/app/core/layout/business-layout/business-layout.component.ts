import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
    selector: 'app-business-layout',
    standalone: true,
    imports: [RouterOutlet, SidebarComponent, TopbarComponent],
    templateUrl: './business-layout.component.html',
    styleUrl: './business-layout.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BusinessLayoutComponent {}