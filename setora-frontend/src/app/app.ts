import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { ThemeService } from './core/services/theme.service';
import { TopbarComponent } from './core/layout/topbar/topbar.component';
import { BusinessService } from './core/services/business.service';

@Component({
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('setora-frontend');
  private readonly themeService = inject(ThemeService);
  private readonly businessService = inject(BusinessService);

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
}