import { BusinessLocation } from './business-location.model';

export interface Business {
  id: number;
  ownerId: number;
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
