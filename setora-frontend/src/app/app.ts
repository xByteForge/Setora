import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TopbarComponent } from './core/layout/topbar/topbar.component';
import { BusinessService } from './core/services/business.service';

@Component({
  imports: [RouterOutlet, TopbarComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly businessService = inject(BusinessService);

  constructor() {
    this.businessService.setCurrent({
      id: '1',
      name: 'Test Business',
      description: '',
      tags: [],
      phone: '',
      email: '',
    });
  }
}