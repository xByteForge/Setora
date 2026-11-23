import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { BusinessCategory } from '../models/business-category.model';
import { Business } from '../models/business.model';
import { ServiceType } from '../models/service-type.model';
import { PageResult } from '../ui-models/page-result.model';
import { BusinessListItem } from '../ui-models/business-list-item.model';
import { BusinessDetail } from '../ui-models/business-detail.model';
import {
  BusinessQuery,
  BusinessSort,
  DEFAULT_BUSINESS_QUERY,
} from '../ui-models/business-query.model';

const SORT_MAP: Record<BusinessSort, string> = {
  newest: 'NEWEST',
  oldest: 'OLDEST',
  nameAsc: 'NAME_ASC',
  nameDesc: 'NAME_DESC',
  priceAsc: 'PRICE_ASC',
  priceDesc: 'PRICE_DESC',
};

@Injectable({ providedIn: 'root' })
export class BusinessDiscoveryService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = 'http://localhost:8080/api';

  private readonly categoryList = signal<BusinessCategory[]>([]);
  private readonly serviceTypeList = signal<ServiceType[]>([]);

  readonly cities = signal<string[]>([]);
  readonly categories = computed(() => this.categoryList().map((c) => c.name));
  readonly serviceOptions = computed(() =>
    this.serviceTypeList().map((st) => ({ id: st.id, name: st.name })),
  );

  readonly query = signal<BusinessQuery>({ ...DEFAULT_BUSINESS_QUERY });
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  private readonly quickSearchResults = signal<BusinessListItem[]>([]);
  private lastQuickSearchQuery = '';
  private quickSearchSub: { unsubscribe(): void } | null = null;

  readonly result = signal<PageResult<BusinessListItem>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: DEFAULT_BUSINESS_QUERY.pageSize,
  });

  readonly activeFilterCount = computed(() => {
    const q = this.query();
    return [
      q.category !== null,
      q.serviceId !== null,
      q.minPrice !== null || q.maxPrice !== null,
      q.date !== null,
      q.timeFrom !== null || q.timeTo !== null,
    ].filter(Boolean).length;
  });

  constructor() {
    this.loadFilterMetadata();

    effect((onCleanup) => {
      const q = this.query();
      const categories = this.categoryList();
      const params = this.buildParams(q, categories);

      this.loading.set(true);
      this.error.set(null);

      const sub = this.http
        .get<PageResult<BusinessListItem>>(`${this.apiBase}/businesses`, { params })
        .subscribe({
          next: (page) => {
            this.result.set(page);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('Failed to load business list.');
            this.loading.set(false);
          },
        });

      onCleanup(() => sub.unsubscribe());
    });
  }

  getById(id: number): Observable<BusinessDetail> {
    return this.http.get<BusinessDetail>(`${this.apiBase}/businesses/${id}`);
  }

  servicesByCategory(category: string | null): { id: number; name: string }[] {
    if (!category) {
      return this.serviceOptions();
    }
    const matched = this.categoryList().find((c) => c.name === category);
    if (!matched) {
      return [];
    }
    return this.serviceTypeList()
      .filter((st) => st.categoryId === matched.id)
      .map((st) => ({ id: st.id, name: st.name }));
  }

  searchByText(raw: string): Observable<BusinessListItem[]> {
    const text = raw.trim();
    if (!text) {
      return of([]);
    }
    const params = new HttpParams().set('text', text).set('page', 0).set('size', 6);
    return this.http
      .get<PageResult<BusinessListItem>>(`${this.apiBase}/businesses`, { params })
      .pipe(map((page) => page.content));
  }

  quickSearch(raw: string): BusinessListItem[] {
    const text = raw.trim();
    if (!text) {
      this.lastQuickSearchQuery = '';
      if (this.quickSearchResults().length > 0) {
        queueMicrotask(() => this.quickSearchResults.set([]));
      }
      return [];
    }

    if (text !== this.lastQuickSearchQuery) {
      this.lastQuickSearchQuery = text;
      queueMicrotask(() => {
        this.quickSearchSub?.unsubscribe();
        this.quickSearchSub = this.searchByText(text).subscribe({
          next: (items) => this.quickSearchResults.set(items),
          error: () => this.quickSearchResults.set([]),
        });
      });
    }

    return this.quickSearchResults();
  }

  count(q: BusinessQuery): Observable<number> {
    const params = this.buildParams({ ...q, page: 1, pageSize: 1 }, this.categoryList());
    return this.http
      .get<PageResult<Business>>(`${this.apiBase}/businesses`, { params })
      .pipe(map((page) => page.totalElements));
  }

  applyFilters(patch: Partial<BusinessQuery>) {
    this.query.update((q) => ({ ...q, ...patch, page: 1 }));
  }

  clearFilters() {
    this.applyFilters({
      text: '',
      category: null,
      serviceId: null,
      minPrice: null,
      maxPrice: null,
      date: null,
      timeFrom: null,
      timeTo: null,
    });
  }

  setPage(page: number) {
    this.query.update((q) => ({ ...q, page }));
  }

  setPageSize(pageSize: number) {
    this.query.update((q) => ({ ...q, pageSize, page: 1 }));
  }

  reset() {
    this.query.set({ ...DEFAULT_BUSINESS_QUERY });
  }

  private loadFilterMetadata() {
    this.http
      .get<string[]>(`${this.apiBase}/businesses/locations`)
      .subscribe((data) => this.cities.set(data));

    this.http
      .get<BusinessCategory[]>(`${this.apiBase}/businesses/categories`)
      .subscribe((data) => this.categoryList.set(data));

    this.http
      .get<ServiceType[]>(`${this.apiBase}/services/types`)
      .subscribe((data) => this.serviceTypeList.set(data));
  }

  private buildParams(q: BusinessQuery, categories: BusinessCategory[]): HttpParams {
    let params = new HttpParams()
      .set('page', Math.max(0, q.page - 1))
      .set('size', q.pageSize)
      .set('sortBy', SORT_MAP[q.sort]);

    if (q.text.trim()) params = params.set('text', q.text.trim());
    if (q.city) params = params.set('location', q.city);

    if (q.category) {
      const cat = categories.find((c) => c.name === q.category);
      if (cat) params = params.set('categoryId', cat.id);
    }

    if (q.serviceId != null) {
      params = params.set('serviceTypeId', q.serviceId);
    }
    if (q.minPrice != null) {
      params = params.set('minPrice', q.minPrice);
    }
    if (q.maxPrice != null) {
      params = params.set('maxPrice', q.maxPrice);
    }
    if (q.date) {
      params = params.set('date', q.date);
    }
    if (q.timeFrom != null) {
      params = params.set('timeFrom', this.minutesToTime(q.timeFrom));
    }
    if (q.timeTo != null) {
      params = params.set('timeTo', this.minutesToTime(q.timeTo));
    }

    return params;
  }

  private minutesToTime(totalMinutes: number): string {
    const h = Math.floor(totalMinutes / 60)
      .toString()
      .padStart(2, '0');
    const m = (totalMinutes % 60).toString().padStart(2, '0');
    return `${h}:${m}`;
  }
}
