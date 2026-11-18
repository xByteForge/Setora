import { Business } from '../models/business.model';
import { Service } from '../models/service.model';

export interface BusinessListItem extends Business {
    category: string;
    city: string;
    minPrice: number;
    services: Service[];
}