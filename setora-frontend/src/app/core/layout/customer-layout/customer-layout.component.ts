import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CustomerSidebarComponent } from '../customer-sidebar/customer-sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
    selector: 'app-customer-layout',
    standalone: true,
    imports: [RouterOutlet, CustomerSidebarComponent, TopbarComponent],
    templateUrl: './customer-layout.component.html',
    styleUrl: './customer-layout.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerLayoutComponent {}