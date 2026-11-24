import { Service } from '../models/service.model';
import { Business } from '../models/business.model';

export interface BusinessDetail extends Business {
  services: Service[];
}
