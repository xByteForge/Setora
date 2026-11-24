import { Injectable } from '@angular/core';
import { Business } from '../models/business.model';
import { Service } from '../models/service.model';

/** Moneda e aceeasi pentru toate businessurile deocamdata. */
export const CURRENCY = 'lei';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly business: Business = {
    id: 1,
    ownerId: 1,
    category: {
      id: 1,
      name: 'bartane',
    },
    name: 'Studio Armonia',
    slug: 'studio-armonia',
    description: 'Spațiu business · București',
    location: {
      locationName: 'Centru',
      address: 'București, Centru',
      latitude: null,
      longitude: null,
    },
    phone: null,
    email: 'contact@studioarmonia.ro',
    timezone: 'Europe/Bucharest',
    createdAt: '2026-01-01T00:00:00Z',
    photoUrls: [],
    thumbnailUrl: null,
    tags: [],
  };

  private readonly services: Service[] = [
    {
      id: 1,
      businessId: 1,
      name: 'Tuns & coafat',
      description: null,
      type: {
        id: 1,
        name: '1',
        categoryId: null,
      },
      price: 150,
      durationMinutes: 60,
      active: true,
      paymentMethod: 'PHYSICAL',
    },
    {
      id: 2,
      businessId: 1,
      name: 'Vopsit & coafat',
      description: null,
      type: {
        id: 1,
        name: '1',
        categoryId: null,
      },
      price: 250,
      durationMinutes: 90,
      active: true,
      paymentMethod: 'PHYSICAL',
    },
    {
      id: 3,
      businessId: 1,
      name: 'Tuns & barbă',
      description: null,
      type: {
        id: 1,
        name: '1',
        categoryId: null,
      },
      price: 120,
      durationMinutes: 60,
      active: true,
      paymentMethod: 'PHYSICAL',
    },
    {
      id: 4,
      businessId: 1,
      name: 'Manichiură',
      description: null,
      type: {
        id: 1,
        name: '1',
        categoryId: null,
      },
      price: 100,
      durationMinutes: 60,
      active: true,
      paymentMethod: 'PHYSICAL',
    },
  ];

  getBusiness(): Business {
    return this.business;
  }

  getServices(): Service[] {
    return this.services;
  }
}
