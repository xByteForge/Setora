export type BusinessSort = 'relevance' | 'priceAsc' | 'priceDesc' | 'nameAsc';

export interface BusinessQuery {
  text: string;
  city: string;
  category: string | null;
  serviceId: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  date: string | null; // YYYY-MM-DD
  timeFrom: number | null; // minute de la miezul nopții
  timeTo: number | null;
  sort: BusinessSort;
  page: number;
  pageSize: number;
}

export const DEFAULT_BUSINESS_QUERY: BusinessQuery = {
  text: '',
  city: 'București',
  category: null,
  serviceId: null,
  minPrice: null,
  maxPrice: null,
  date: null,
  timeFrom: null,
  timeTo: null,
  sort: 'relevance',
  page: 1,
  pageSize: 6,
};
