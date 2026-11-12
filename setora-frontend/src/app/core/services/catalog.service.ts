import { Injectable } from '@angular/core';
import { Location } from '../models/location.model';
import { Service } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class CatalogService {
    private readonly locations: Location[] = [
        { id: 'buc-centru', name: 'București · Centru' },
        { id: 'buc-nord', name: 'București · Nord' },
    ];

    private readonly services: Service[] = [
        { id: 'tuns-coafat', name: 'Tuns & coafat', durationMinutes: 60, price: 150, currency: 'lei', paymentMethod: 'Physical' },
        { id: 'vopsit-coafat', name: 'Vopsit & coafat', durationMinutes: 90, price: 250, currency: 'lei', paymentMethod: 'Physical' },
        { id: 'tuns-barba', name: 'Tuns & barbă', durationMinutes: 60, price: 120, currency: 'lei', paymentMethod: 'Physical' },
        { id: 'manichiura', name: 'Manichiură', durationMinutes: 60, price: 100, currency: 'lei', paymentMethod: 'Physical' },
    ];

    getLocations(): Location[] {
        return this.locations;
    }

    getServices(): Service[] {
        return this.services;
    }
}