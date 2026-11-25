import { AbstractControl, ValidationErrors } from '@angular/forms';
import { BusinessCategory } from '../../core/models/business-category.model';

/** Pre-fills the form with the demo data from the design. Set to false for production. */
export const PREFILL_DEMO = true;

/** URL handles that are already taken (demo of the validation state). */
export const TAKEN_HANDLES = ['studio-verde', 'salon-luna', 'beauty-hub'];

export const SECTION_IDS = [
  'section-profile',
  'section-photos',
  'section-contact',
  'section-location',
  'section-verification',
] as const;
export type SectionId = (typeof SECTION_IDS)[number];

export const BUSINESS_CATEGORIES: BusinessCategory[] = [
  { id: 1, name: 'BEAUTY' },
  { id: 2, name: 'HEALTH' },
  { id: 3, name: 'FITNESS' },
  { id: 4, name: 'AUTO' },
];

export const TAG_IDS = [
  'HAIRSTYLE',
  'STYLING',
  'MANICURE',
  'PEDICURE',
  'MASSAGE',
  'BARBER',
  'MAKEUP',
  'FACIAL',
  'SPA',
  'PERSONAL_TRAINING',
  'YOGA',
  'PHYSIOTHERAPY',
  'DENTAL',
  'CAR_WASH',
];

export interface CityInfo {
  id: string;
  slug: string;
  lat: number;
  lng: number;
}

export interface CountryInfo {
  id: string;
  dial: string;
  timezone: string;
  cities: CityInfo[];
}

export const COUNTRIES: CountryInfo[] = [
  {
    id: 'RO',
    dial: '+40',
    timezone: 'Europe/Bucharest',
    cities: [
      { id: 'BUCHAREST', slug: 'bucuresti', lat: 44.447, lng: 26.098 },
      { id: 'CLUJ_NAPOCA', slug: 'cluj-napoca', lat: 46.77, lng: 23.59 },
      { id: 'TIMISOARA', slug: 'timisoara', lat: 45.756, lng: 21.23 },
      { id: 'IASI', slug: 'iasi', lat: 47.158, lng: 27.601 },
      { id: 'BRASOV', slug: 'brasov', lat: 45.657, lng: 25.601 },
      { id: 'CONSTANTA', slug: 'constanta', lat: 44.18, lng: 28.634 },
    ],
  },
  {
    id: 'MD',
    dial: '+373',
    timezone: 'Europe/Chisinau',
    cities: [
      { id: 'CHISINAU', slug: 'chisinau', lat: 47.01, lng: 28.86 },
      { id: 'BALTI', slug: 'balti', lat: 47.76, lng: 27.93 },
    ],
  },
  {
    id: 'HU',
    dial: '+36',
    timezone: 'Europe/Budapest',
    cities: [
      { id: 'BUDAPEST', slug: 'budapest', lat: 47.498, lng: 19.04 },
      { id: 'DEBRECEN', slug: 'debrecen', lat: 47.53, lng: 21.627 },
    ],
  },
  {
    id: 'BG',
    dial: '+359',
    timezone: 'Europe/Sofia',
    cities: [
      { id: 'SOFIA', slug: 'sofia', lat: 42.698, lng: 23.322 },
      { id: 'PLOVDIV', slug: 'plovdiv', lat: 42.15, lng: 24.75 },
    ],
  },
];

export const TIMEZONES = [
  'Europe/Bucharest',
  'Europe/Chisinau',
  'Europe/Budapest',
  'Europe/Sofia',
  'Europe/Athens',
  'Europe/Berlin',
  'Europe/Rome',
  'Europe/Paris',
  'Europe/Madrid',
  'Europe/London',
];

export const DIAL_CODES = [
  { code: 'RO', dial: '+40' },
  { code: 'MD', dial: '+373' },
  { code: 'HU', dial: '+36' },
  { code: 'BG', dial: '+359' },
  { code: 'DE', dial: '+49' },
  { code: 'FR', dial: '+33' },
  { code: 'IT', dial: '+39' },
  { code: 'ES', dial: '+34' },
  { code: 'GB', dial: '+44' },
];

export const MAX_PHOTOS = 10;
export const MAX_PHOTO_BYTES = 5 * 1024 * 1024;
export const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

export function slugify(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60);
}

const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

export function slugValidator(control: AbstractControl): ValidationErrors | null {
  const value = (control.value as string) ?? '';
  if (!value) return null; // handled by `required`
  if (value.length < 3 || !SLUG_PATTERN.test(value)) return { slugFormat: true };
  return null;
}

export function takenValidator(control: AbstractControl): ValidationErrors | null {
  return TAKEN_HANDLES.includes(control.value) ? { taken: true } : null;
}

/** Inline SVG placeholder so the demo works without image assets. Swap for real URLs. */
export function demoPhoto(top: string, bottom: string, accent: string): string {
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 300" preserveAspectRatio="xMidYMid slice">` +
    `<defs><linearGradient id="g" x1="0" y1="0" x2="0" y2="1">` +
    `<stop offset="0" stop-color="${top}"/><stop offset="1" stop-color="${bottom}"/></linearGradient></defs>` +
    `<rect width="640" height="300" fill="url(#g)"/>` +
    `<rect x="70" y="30" width="110" height="170" rx="55" fill="#ffffff" opacity=".55"/>` +
    `<rect x="0" y="215" width="640" height="85" fill="#000" opacity=".08"/>` +
    `<rect x="300" y="150" width="120" height="70" rx="26" fill="${accent}"/>` +
    `<rect x="470" y="110" width="90" height="12" rx="6" fill="${accent}" opacity=".7"/>` +
    `<circle cx="560" cy="205" r="26" fill="#3f6b4f" opacity=".8"/></svg>`;
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
}

export type FieldName =
  | 'name'
  | 'slug'
  | 'description'
  | 'email'
  | 'phoneCode'
  | 'phone'
  | 'country'
  | 'city'
  | 'address'
  | 'postalCode'
  | 'timezone';

export interface PhotoItem {
  id: string;
  src: string;
  name: string;
  objectUrl: boolean;
}
