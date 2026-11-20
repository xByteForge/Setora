import { Injectable, computed, signal } from '@angular/core';
import { Service } from '../models/service.model';
import { BusinessListItem } from '../ui-models/business-list-item.model';
import { BusinessQuery, DEFAULT_BUSINESS_QUERY } from '../ui-models/business-query.model';
import { PageResult } from '../ui-models/page-result.model';

interface Seed {
  name: string;
  category: string;
  neighborhood: string;
  description: string;
  image: string;
  services: [string, number, number][]; // nume, preț, durata (min)
}

const SEEDS: Seed[] = [
  {
    name: 'Studio Armonia',
    category: 'Coafură & beauty',
    neighborhood: 'Piața Romană',
    description: 'Tuns, coafat și culoare, în ritmul tău.',
    image: 'studio-armonia.jpg',
    services: [
      ['Tuns & coafat', 150, 60],
      ['Vopsit & coafat', 250, 90],
    ],
  },
  {
    name: 'Atelier Gentleman',
    category: 'Frizerie',
    neighborhood: 'Dorobanți',
    description: 'Tunsori și îngrijirea bărbii, cu atenție la detalii.',
    image: 'atelier-gentleman.jpg',
    services: [
      ['Tuns bărbați', 70, 45],
      ['Tuns & barbă', 120, 60],
    ],
  },
  {
    name: 'Flow Pilates',
    category: 'Pilates',
    neighborhood: 'Floreasca',
    description: 'Clase de pilates în grupuri mici.',
    image: 'flow-pilates.jpg',
    services: [
      ['Ședință de grup', 90, 60],
      ['Ședință privată', 250, 60],
    ],
  },
  {
    name: 'Forma Fitness',
    category: 'Fitness',
    neighborhood: 'Tineretului',
    description: 'Antrenamente ghidate pentru obiectivele tale.',
    image: 'forma-fitness.jpg',
    services: [
      ['Acces sală', 60, 60],
      ['Antrenament personal', 150, 60],
    ],
  },
  {
    name: 'Aura Beauty',
    category: 'Îngrijire personală',
    neighborhood: 'Cotroceni',
    description: 'Manichiură și tratamente pentru un moment al tău.',
    image: 'aura-beauty.jpg',
    services: [
      ['Manichiură', 100, 60],
      ['Tratament facial', 220, 75],
    ],
  },
  {
    name: 'Auto Clean Studio',
    category: 'Spălătorie auto',
    neighborhood: 'Aviației',
    description: 'Curățare și îngrijire auto, cu programare.',
    image: 'auto-clean-studio.jpg',
    services: [
      ['Spălat exterior', 80, 30],
      ['Spălat complet', 160, 90],
    ],
  },
];

const EXTRA_NEIGHBORHOODS = [
  'Titan',
  'Militari',
  'Drumul Taberei',
  'Berceni',
  'Pipera',
  'Herăstrău',
  'Unirii',
  'Obor',
  'Băneasa',
  'Victoriei',
  'Cismigiu',
  'Colentina',
];

const slugify = (s: string) =>
  s
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '');

const norm = (s: string) =>
  s
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

@Injectable({ providedIn: 'root' })
export class BusinessDiscoveryService {
  /** 24 in București + 6 in Cluj-Napoca */
  private readonly businesses: BusinessListItem[] = Array.from({ length: 30 }, (_, i) =>
    this.build(i),
  );

  readonly cities = [...new Set(this.businesses.map((b) => b.city))];
  readonly categories = [...new Set(this.businesses.map((b) => b.category))];
  readonly serviceOptions = [
    ...new Map(
      this.businesses.flatMap((b) => b.services).map((s) => [s.id, { id: s.id, name: s.name }]),
    ).values(),
  ];

  readonly query = signal<BusinessQuery>({ ...DEFAULT_BUSINESS_QUERY });

  readonly result = computed<PageResult<BusinessListItem>>(() => {
    const q = this.query();
    const items = this.filtered(q);
    const start = (q.page - 1) * q.pageSize;
    return {
      items: items.slice(start, start + q.pageSize),
      total: items.length,
      page: q.page,
      pageSize: q.pageSize,
    };
  });

  private matchesText(b: BusinessListItem, text: string): boolean {
    return norm(
      [
        b.name,
        b.description ?? '',
        b.category,
        b.city,
        b.location.locationName ?? '',
        ...b.services.map((s) => s.name),
      ].join(' '),
    ).includes(text);
  }

  /** Pentru search-ul universal din topbar. */
  searchByText(raw: string): BusinessListItem[] {
    const text = norm(raw.trim());
    return text ? this.businesses.filter((b) => this.matchesText(b, text)) : [];
  }

  /** Cate filtre din panou sunt active (fara localitate, sortare si text). */
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

  /** Numarul de rezultate pentru un query oarecare (previzualizare in panoul de filtre). */
  count(q: BusinessQuery): number {
    return this.filtered(q).length;
  }

  /** Serviciile din businessurile unei categorii (toate, daca nu e aleasa nicio categorie). */
  servicesByCategory(category: string | null): { id: number; name: string }[] {
    const services = this.businesses
      .filter((b) => !category || b.category === category)
      .flatMap((b) => b.services);
    return [...new Map(services.map((s) => [s.id, { id: s.id, name: s.name }])).values()];
  }

  applyFilters(patch: Partial<BusinessQuery>) {
    this.query.update((q) => ({ ...q, ...patch, page: 1 }));
  }

  /** Sterge filtrele din panou si textul cautat; pastreaza localitatea si sortarea. */
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

  private filtered(q: BusinessQuery): BusinessListItem[] {
    const text = norm(q.text.trim());

    let items = this.businesses.filter(
      (b) =>
        (!q.city || b.city === q.city) &&
        (!q.category || b.category === q.category) &&
        (q.serviceId == null || b.services.some((s) => s.id === q.serviceId)) &&
        b.services.some(
          (s) =>
            (q.minPrice == null || s.price >= q.minPrice) &&
            (q.maxPrice == null || s.price <= q.maxPrice),
        ) &&
        (!text || this.matchesText(b, text)),
    );

    // date / timeFrom / timeTo sunt ignorate în mock (le va rezolva backend-ul prin disponibilitate)
    if (q.sort === 'priceAsc') items = [...items].sort((a, b) => a.minPrice - b.minPrice);
    if (q.sort === 'priceDesc') items = [...items].sort((a, b) => b.minPrice - a.minPrice);
    if (q.sort === 'nameAsc') items = [...items].sort((a, b) => a.name.localeCompare(b.name, 'ro'));

    return items;
  }

  private build(index: number): BusinessListItem {
    const seedIndex = index % SEEDS.length;
    const seed = SEEDS[seedIndex];
    const isOriginal = index < SEEDS.length;
    const city = index < 24 ? 'București' : 'Cluj-Napoca';
    const neighborhood = isOriginal
      ? seed.neighborhood
      : EXTRA_NEIGHBORHOODS[(index - SEEDS.length) % EXTRA_NEIGHBORHOODS.length];
    const name = isOriginal ? seed.name : `${seed.name} ${neighborhood}`;
    const businessId = index + 1;

    const services: Service[] = seed.services.map(([n, price, durationMinutes], j) => ({
      id: seedIndex * 10 + j + 1,
      businessId,
      name: n,
      description: null,
      price,
      durationMinutes,
      active: true,
      paymentMethod: 'PHYSICAL',
    }));

    return {
      id: businessId,
      ownerId: businessId,
      name,
      slug: slugify(name),
      description: seed.description,
      location: {
        locationName: neighborhood,
        address: `${city}, ${neighborhood}`,
        latitude: null,
        longitude: null,
      },
      phone: null,
      email: `contact@${slugify(name)}.ro`,
      timezone: 'Europe/Bucharest',
      createdAt: '2026-01-01T00:00:00Z',
      photoUrls: [],
      thumbnailUrl: `assets/mock/${seed.image}`,
      tags: [seed.category],
      category: seed.category,
      city,
      minPrice: Math.min(...services.map((s) => s.price)),
      services,
    };
  }
}
