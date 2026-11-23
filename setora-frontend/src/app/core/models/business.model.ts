import { BusinessLocation } from './business-location.model';
import { BusinessCategory } from "./business-category.model";

export interface Business {
  id: number;
  ownerId: number;
  category: BusinessCategory;
  name: string;
  slug: string;
  description: string | null;
  location: BusinessLocation;
  phone: string | null;
  email: string;
  timezone: string;
  createdAt: string;
  photoUrls: string[];
  thumbnailUrl: string | null;
  tags: string[];
}
