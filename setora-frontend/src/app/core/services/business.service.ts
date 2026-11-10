import { Injectable, signal } from '@angular/core';
import { Business } from '../models/business.model';

@Injectable({ providedIn: 'root' })
export class BusinessService {
  readonly current = signal<Business | null>(null);

  setCurrent(business: Business | null): void {
    this.current.set(business);
  }
}