import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { ThemeService } from './core/services/theme.service';
import { TopbarComponent } from './core/layout/topbar/topbar.component';
import { BusinessService } from './core/services/business.service';
import {AppointmentsHeaderComponent} from "./core/layout/appointment-header/appointment-header.component";
import {AppointmentSummary} from "./core/models/appointment-summary.model";

@Component({
  imports: [RouterOutlet, SidebarComponent, TopbarComponent, AppointmentsHeaderComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('setora-frontend');
  private readonly themeService = inject(ThemeService);
  private readonly businessService = inject(BusinessService);

  readonly summary = signal<AppointmentSummary>({
    total: 9,
    confirmed: 7,
    pending: 1,
    cancelled: 1,
  });

  constructor() {
    this.businessService.setCurrent({
      id: '1',
      name: 'Studio Armonia',
      description: 'Spațiu business · București',
      tags: [],
      phone: '',
      email: '',
    });
  }

  openAddAppointment(): void {
    // TODO: deschide formularul de adăugare
  }
}