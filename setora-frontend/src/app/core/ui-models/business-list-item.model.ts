import { Business } from '../models/business.model';

export interface BusinessListItem extends Omit<Business, 'category'> {
  category: string;
  city: string;
  minPrice: number;
}
