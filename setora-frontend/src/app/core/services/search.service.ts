import { Injectable, inject } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
import { CatalogService, CURRENCY } from './catalog.service';
import { SearchGroup, SearchResult } from '../models/search-results.model';
import { BusinessDiscoveryService } from './business-discovery.service';

const MAX_PER_GROUP = 5;

const PAGES = [
  { labelKey: 'NAV.DISCOVER', url: '/discover' },
  { labelKey: 'NAV.APPOINTMENTS', url: '/appointments' },
  { labelKey: 'NAV.SERVICES', url: '/services' },
  { labelKey: 'NAV.EMPLOYEES', url: '/employees' },
  { labelKey: 'NAV.CLIENTS', url: '/clients' },
  { labelKey: 'NAV.LOCATIONS', url: '/locations' },
  { labelKey: 'NAV.WORKING_HOURS', url: '/working-hours' },
  { labelKey: 'NAV.SETTINGS', url: '/settings' },
];

type Matcher = (...fields: (string | null | undefined)[]) => boolean;

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly discovery = inject(BusinessDiscoveryService);
  private readonly catalog = inject(CatalogService);
  private readonly transloco = inject(TranslocoService);

  search(raw: string): SearchGroup[] {
    const q = this.normalize(raw);
    if (!q) return [];

    const match: Matcher = (...fields) => fields.some((f) => !!f && this.normalize(f).includes(q));

    return [
      this.group('pages', 'SEARCH.PAGES', this.searchPages(match)),
      this.group('businesses', 'SEARCH.BUSINESSES', this.searchBusinesses(raw)),
      this.group('services', 'SEARCH.SERVICES', this.searchServices(match)),
    ].filter((g) => g.items.length > 0);
  }

  private searchPages(match: Matcher): SearchResult[] {
    return PAGES.map((p) => ({ ...p, title: this.transloco.translate(p.labelKey) }))
      .filter((p) => match(p.title))
      .map((p) => ({
        id: `page:${p.url}`,
        category: 'pages' as const,
        title: p.title,
        url: p.url,
      }));
  }

  private searchBusinesses(raw: string): SearchResult[] {
    return this.discovery.quickSearch(raw).map((b) => ({
      id: `business:${b.id}`,
      category: 'businesses' as const,
      title: b.name,
      subtitle: `${b.category} · ${b.location.locationName ?? b.city}`,
      url: `/discover?q=${encodeURIComponent(b.name)}`,
    }));
  }

  private searchServices(match: Matcher): SearchResult[] {
    return this.catalog
      .getServices()
      .filter((s) => s.active && match(s.name, s.description))
      .map((s) => ({
        id: `service:${s.id}`,
        category: 'services' as const,
        title: s.name,
        subtitle: `${s.price} ${CURRENCY} · ${s.durationMinutes} min`,
        url: '/services',
      }));
  }

  private group(
    category: SearchGroup['category'],
    labelKey: string,
    items: SearchResult[],
  ): SearchGroup {
    return { category, labelKey, items: items.slice(0, MAX_PER_GROUP) };
  }

  // fara diacritice, case-insensitive: „bucuresti" gaseste „București"
  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
