import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { Observable, map, startWith } from 'rxjs';
import { Business } from '../../core/models/business.model';
import { BusinessCategory } from '../../core/models/business-category.model';
import { BusinessLocation } from '../../core/models/business-location.model';
import {
  ACCEPTED_TYPES,
  BUSINESS_CATEGORIES,
  COUNTRIES,
  DIAL_CODES,
  FieldName,
  MAX_PHOTOS,
  MAX_PHOTO_BYTES,
  PREFILL_DEMO,
  PhotoItem,
  SECTION_IDS,
  SectionId,
  TAG_IDS,
  TAKEN_HANDLES,
  TIMEZONES,
  demoPhoto,
  slugValidator,
  slugify,
  takenValidator,
} from './business-registration.util';

@Component({
  selector: 'app-business-registration',
  standalone: true,
  imports: [TranslocoModule, ReactiveFormsModule],
  templateUrl: './business-registration.component.html',
  styleUrl: './business-registration.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BusinessRegistrationComponent implements AfterViewInit {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly doc = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly transloco = inject(TranslocoService);
  private readonly fb = inject(NonNullableFormBuilder);

  // static data exposed to the template
  readonly sectionIds = SECTION_IDS;
  readonly categories: BusinessCategory[] = BUSINESS_CATEGORIES;
  readonly countries = COUNTRIES;
  readonly timezones = TIMEZONES;
  readonly dialCodes = DIAL_CODES;

  /* ---------------- form ---------------- */

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    slug: ['', [Validators.required, slugValidator, takenValidator]],
    description: ['', [Validators.maxLength(500)]],
    email: ['', [Validators.required, Validators.email]],
    phoneCode: ['+40'],
    phone: ['', [Validators.pattern(/^[0-9][0-9 .-]{5,14}$/)]],
    country: ['RO', [Validators.required]],
    city: ['', [Validators.required]],
    address: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    postalCode: ['', [Validators.pattern(/^[A-Za-z0-9][A-Za-z0-9 -]{2,9}$/)]],
    timezone: ['Europe/Bucharest', [Validators.required]],
  });

  readonly values = toSignal(
    this.form.valueChanges.pipe(
      map(() => this.form.getRawValue()),
      startWith(this.form.getRawValue()),
    ),
    { initialValue: this.form.getRawValue() },
  );

  readonly submitted = signal(false);
  readonly registered = signal(false);

  /* ---------------- other state ---------------- */

  readonly selectedCategory = signal<BusinessCategory | null>(null);
  readonly tags = signal<string[]>([]);
  readonly photos = signal<PhotoItem[]>([]);
  readonly galleryIds = signal<string[]>([]);
  readonly coverId = signal<string | null>(null);
  readonly photoError = signal<'type' | 'size' | 'max' | null>(null);
  readonly dragOver = signal(false);

  readonly tagQuery = signal('');
  readonly tagOpen = signal(false);
  readonly tagActive = signal(0);

  readonly pin = signal({ x: 50, y: 48 });
  readonly zoom = signal(1);
  readonly adjusting = signal(false);
  readonly timezoneManual = signal(false);

  readonly activeSection = signal<SectionId>('section-profile');

  private slugManual = false;
  private phoneCodeManual = false;

  /* ---------------- i18n-driven signals ---------------- */

  private readonly tagLabels = toSignal(
    this.transloco.selectTranslateObject('BUSINESS_REGISTRATION.TAG_OPTIONS') as Observable<
      Record<string, string>
    >,
    { initialValue: {} as Record<string, string> },
  );

  /* ---------------- derived state ---------------- */

  readonly currentCountry = computed(
    () => COUNTRIES.find((c) => c.id === this.values().country) ?? COUNTRIES[0],
  );

  readonly cities = computed(() => this.currentCountry().cities);

  readonly currentCity = computed(
    () => this.cities().find((c) => c.id === this.values().city) ?? null,
  );

  readonly slugTaken = computed(() => TAKEN_HANDLES.includes(this.values().slug));

  readonly suggestion = computed(() => {
    const slug = this.values().slug;
    if (!this.slugTaken()) return null;
    const citySlug = this.currentCity()?.slug;
    const candidates = [citySlug ? `${slug}-${citySlug}` : null, `${slug}-2`, `${slug}-3`].filter(
      (c): c is string => !!c,
    );
    return candidates.find((c) => !TAKEN_HANDLES.includes(c)) ?? null;
  });

  /** True when the handle blocks registration (taken, malformed, empty). */
  readonly slugInvalid = computed(() => {
    this.values();
    return this.form.controls.slug.invalid;
  });

  readonly availableTags = computed(() => {
    const selected = new Set(this.tags());
    const q = this.normalize(this.tagQuery());
    return TAG_IDS.filter((id) => !selected.has(id))
      .map((id) => ({ id, label: this.tagLabels()[id] ?? id }))
      .filter((t) => !q || this.normalize(t.label).includes(q));
  });

  /** Offers to add the typed text as a custom tag when no exact match exists. */
  readonly customTagCandidate = computed(() => {
    const raw = this.tagQuery().trim();
    if (raw.length < 2) return null;
    const q = this.normalize(raw);
    const exists =
      this.tags().some((t) => this.normalize(this.tagLabel(t)) === q) ||
      TAG_IDS.some((id) => this.normalize(this.tagLabels()[id] ?? id) === q);
    return exists ? null : raw;
  });

  readonly tagOptionCount = computed(
    () => this.availableTags().length + (this.customTagCandidate() ? 1 : 0),
  );

  readonly coverPhoto = computed(() => {
    const photos = this.photos();
    return photos.find((p) => p.id === this.coverId()) ?? photos[0] ?? null;
  });

  readonly thumbnails = computed(() => {
    const cover = this.coverPhoto();
    const byId = new Map(this.photos().map((p) => [p.id, p]));
    return this.galleryIds()
      .map((id) => byId.get(id))
      .filter((p): p is PhotoItem => !!p && p.id !== cover?.id)
      .slice(0, 2);
  });

  readonly location = computed<BusinessLocation>(() => {
    const city = this.currentCity() ?? COUNTRIES[0].cities[0];
    const p = this.pin();
    const scale = 0.0004 / this.zoom();
    const address = this.values().address.trim();
    return {
      locationName: city.id,
      address: address || null,
      latitude: city.lat - (p.y - 48) * scale,
      longitude: city.lng + (p.x - 50) * scale,
    };
  });

  readonly streetLabel = computed(() => {
    const street = this.values()
      .address.replace(/\s*\d.*$/, '')
      .trim();
    return street || '';
  });

  readonly phoneDisplay = computed(() => {
    const { phone, phoneCode } = this.values();
    return phone.trim() ? `${phoneCode} ${phone.trim()}` : '';
  });

  previewAddress(cityName: string, countryName: string): string {
    const v = this.values();
    const cityLine = [v.city ? cityName : '', v.postalCode.trim()].filter(Boolean).join(' ');
    return [v.address.trim(), cityLine, v.country ? countryName : ''].filter(Boolean).join(', ');
  }

  /* ---------------- lifecycle ---------------- */

  constructor() {
    if (PREFILL_DEMO) this.applyDemo();
    else this.form.controls.city.setValue(COUNTRIES[0].cities[0].id);

    // auto-generate the handle from the business name until the user edits it
    this.form.controls.name.valueChanges.pipe(takeUntilDestroyed()).subscribe((name) => {
      if (!this.slugManual) this.form.controls.slug.setValue(slugify(name));
    });

    // country change: reset city, sync timezone + dial code
    this.form.controls.country.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      const country = this.currentCountry();
      this.form.controls.city.setValue(country.cities[0].id);
      if (!this.timezoneManual()) this.form.controls.timezone.setValue(country.timezone);
      if (!this.phoneCodeManual) this.form.controls.phoneCode.setValue(country.dial);
    });

    // city change: recentre the pin
    this.form.controls.city.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.pin.set({ x: 50, y: 48 });
      this.zoom.set(1);
    });

    this.destroyRef.onDestroy(() => {
      this.photos().forEach((p) => p.objectUrl && URL.revokeObjectURL(p.src));
    });
  }

  ngAfterViewInit(): void {
    this.observeSections();
  }

  private applyDemo(): void {
    this.slugManual = true;
    this.form.patchValue(
      {
        name: 'Studio Verde',
        slug: 'studio-verde',
        description:
          'Un salon luminos, unde timpul pentru tine începe cu un tuns, un coafat și un moment de liniște.',
        email: 'contact@studioverde.example',
        phone: '700 000 456',
        country: 'RO',
        city: 'BUCHAREST',
        address: 'Bd. Dacia 34',
        postalCode: '010403',
        timezone: 'Europe/Bucharest',
      },
      { emitEvent: false },
    );
    this.selectedCategory.set(BUSINESS_CATEGORIES[0]);
    this.tags.set(['HAIRSTYLE', 'STYLING']);
    const demo: PhotoItem[] = [
      {
        id: 'demo-1',
        name: 'demo-1',
        objectUrl: false,
        src: demoPhoto('#efe6d6', '#cdb99b', '#f4eee3'),
      },
      {
        id: 'demo-2',
        name: 'demo-2',
        objectUrl: false,
        src: demoPhoto('#e6dfcf', '#b9a37f', '#e9dfcb'),
      },
      {
        id: 'demo-3',
        name: 'demo-3',
        objectUrl: false,
        src: demoPhoto('#dfe4d6', '#a9b196', '#ece5d3'),
      },
    ];
    this.photos.set(demo);
    this.coverId.set('demo-1');
    this.galleryIds.set(['demo-2', 'demo-3']);
  }

  /* ---------------- generic field helpers ---------------- */

  showError(name: FieldName): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || this.submitted());
  }

  /** Handle errors show immediately for a taken handle, otherwise after touch/submit. */
  get slugError(): 'required' | 'format' | 'taken' | null {
    const c = this.form.controls.slug;
    if (c.hasError('taken')) return 'taken';
    if (!(c.touched || this.submitted())) return null;
    if (c.hasError('required')) return 'required';
    if (c.hasError('slugFormat')) return 'format';
    return null;
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  /* ---------------- handle ---------------- */

  onSlugInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const cleaned = input.value.toLowerCase().replace(/\s+/g, '-');
    if (cleaned !== input.value) {
      input.value = cleaned;
      this.form.controls.slug.setValue(cleaned);
    }
    this.slugManual = cleaned.length > 0;
    if (!this.slugManual) this.form.controls.slug.setValue(slugify(this.form.controls.name.value));
  }

  applySuggestion(): void {
    const s = this.suggestion();
    if (!s) return;
    this.slugManual = true;
    this.form.controls.slug.setValue(s);
    this.form.controls.slug.markAsTouched();
  }

  /* ---------------- categories ---------------- */

  toggleCategory(category: BusinessCategory): void {
    this.selectedCategory.update((current) => (current?.id === category.id ? null : category));
  }

  /* ---------------- tags ---------------- */

  tagLabel(tag: string): string {
    return this.tagLabels()[tag] ?? tag;
  }

  openTags(): void {
    this.tagOpen.set(true);
  }

  onTagInput(event: Event): void {
    this.tagQuery.set((event.target as HTMLInputElement).value);
    this.tagActive.set(0);
    this.tagOpen.set(true);
  }

  addTag(tag: string): void {
    if (!this.tags().includes(tag)) {
      this.tags.update((list) => [...list, tag]);
    }
    this.resetTagInput();
  }

  addCustomTag(label: string): void {
    const clean = label.trim().slice(0, 30);
    if (!clean) return;
    this.addTag(clean);
  }

  removeTag(tag: string): void {
    this.tags.update((list) => list.filter((t) => t !== tag));
  }

  private resetTagInput(): void {
    this.tagQuery.set('');
    this.tagActive.set(0);
    const input = this.host.nativeElement.querySelector<HTMLInputElement>('#tag-search');
    if (input) {
      input.value = '';
      input.focus();
    }
  }

  onTagKeydown(event: KeyboardEvent): void {
    const count = this.tagOptionCount();
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.tagOpen.set(true);
        if (count) this.tagActive.set((this.tagActive() + 1) % count);
        break;
      case 'ArrowUp':
        event.preventDefault();
        if (count) this.tagActive.set((this.tagActive() - 1 + count) % count);
        break;
      case 'Enter': {
        event.preventDefault();
        this.chooseTagAt(this.tagActive());
        break;
      }
      case 'Escape':
        this.tagOpen.set(false);
        break;
      case 'Backspace':
        if (!this.tagQuery() && this.tags().length) {
          this.removeTag(this.tags()[this.tags().length - 1]);
        }
        break;
    }
  }

  chooseTagAt(index: number): void {
    const list = this.availableTags();
    if (index < list.length) this.addTag(list[index].id);
    else if (this.customTagCandidate()) this.addCustomTag(this.customTagCandidate()!);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as Node;
    const box = this.host.nativeElement.querySelector('.tag-combobox');
    if (box && !box.contains(target)) this.tagOpen.set(false);
  }

  /* ---------------- photos ---------------- */

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) this.addFiles(Array.from(input.files));
    input.value = '';
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(false);
    if (event.dataTransfer?.files) this.addFiles(Array.from(event.dataTransfer.files));
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(true);
  }

  private addFiles(files: File[]): void {
    this.photoError.set(null);
    const added: PhotoItem[] = [];
    for (const file of files) {
      if (!ACCEPTED_TYPES.includes(file.type)) {
        this.photoError.set('type');
        continue;
      }
      if (file.size > MAX_PHOTO_BYTES) {
        this.photoError.set('size');
        continue;
      }
      if (this.photos().length + added.length >= MAX_PHOTOS) {
        this.photoError.set('max');
        break;
      }
      added.push({
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        name: file.name,
        src: URL.createObjectURL(file),
        objectUrl: true,
      });
    }
    if (!added.length) return;
    this.photos.update((list) => [...list, ...added]);
    this.galleryIds.update((l) => [...l, ...added.map((p) => p.id)].slice(0, 2));
    if (!this.coverId()) this.coverId.set(added[0].id);
  }

  setCover(id: string): void {
    this.coverId.set(id);
    this.galleryIds.update((l) => l.filter((g) => g !== id));
  }

  toggleGallery(id: string): void {
    this.galleryIds.update((l) =>
      l.includes(id) ? l.filter((g) => g !== id) : [...l, id].slice(-2),
    );
  }

  removePhoto(photo: PhotoItem): void {
    if (photo.objectUrl) URL.revokeObjectURL(photo.src);
    this.photos.update((list) => list.filter((p) => p.id !== photo.id));
    this.galleryIds.update((l) => l.filter((g) => g !== photo.id));
    if (this.coverId() === photo.id) this.coverId.set(this.photos()[0]?.id ?? null);
    this.photoError.set(null);
  }

  /* ---------------- phone / timezone ---------------- */

  onPhoneCodeChange(): void {
    this.phoneCodeManual = true;
  }

  onTimezoneChange(): void {
    this.timezoneManual.set(true);
  }

  /** "UTC+02:00" for the given IANA zone at the current date. */
  tzOffset(zone: string): string {
    try {
      const parts = new Intl.DateTimeFormat('en', {
        timeZone: zone,
        timeZoneName: 'longOffset',
      }).formatToParts(new Date());
      const raw = parts.find((p) => p.type === 'timeZoneName')?.value ?? 'GMT';
      return raw === 'GMT' ? 'UTC+00:00' : raw.replace('GMT', 'UTC');
    } catch {
      return 'UTC';
    }
  }

  tzKey(zone: string): string {
    return zone.replace('/', '_').toUpperCase();
  }

  /* ---------------- map pin ---------------- */

  toggleAdjust(): void {
    this.adjusting.update((v) => !v);
  }

  zoomIn(): void {
    this.zoom.update((z) => Math.min(2, +(z + 0.25).toFixed(2)));
  }

  zoomOut(): void {
    this.zoom.update((z) => Math.max(1, +(z - 0.25).toFixed(2)));
  }

  onMapPointerDown(event: PointerEvent, map: HTMLElement): void {
    if (!this.adjusting()) return;
    event.preventDefault();
    map.setPointerCapture(event.pointerId);
    this.movePin(event, map);
  }

  onMapPointerMove(event: PointerEvent, map: HTMLElement): void {
    if (!this.adjusting() || !map.hasPointerCapture(event.pointerId)) return;
    this.movePin(event, map);
  }

  private movePin(event: PointerEvent, map: HTMLElement): void {
    const rect = map.getBoundingClientRect();
    const x = Math.min(95, Math.max(5, ((event.clientX - rect.left) / rect.width) * 100));
    const y = Math.min(90, Math.max(12, ((event.clientY - rect.top) / rect.height) * 100));
    this.pin.set({ x, y });
  }

  onPinKeydown(event: KeyboardEvent): void {
    const step = 2;
    const { x, y } = this.pin();
    const moves: Record<string, [number, number]> = {
      ArrowLeft: [-step, 0],
      ArrowRight: [step, 0],
      ArrowUp: [0, -step],
      ArrowDown: [0, step],
    };
    const move = moves[event.key];
    if (!move) return;
    event.preventDefault();
    this.pin.set({
      x: Math.min(95, Math.max(5, x + move[0])),
      y: Math.min(90, Math.max(12, y + move[1])),
    });
  }

  /* ---------------- navigation between sections ---------------- */

  scrollTo(id: string): void {
    const el = this.doc.getElementById(id);
    if (!el) return;
    const reduce = this.doc.defaultView?.matchMedia('(prefers-reduced-motion: reduce)').matches;
    el.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'start' });
    this.activeSection.set(id as SectionId);
  }

  onJump(event: Event): void {
    this.scrollTo((event.target as HTMLSelectElement).value);
  }

  private observeSections(): void {
    if (typeof IntersectionObserver === 'undefined') return;
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)[0];
        if (visible) this.activeSection.set(visible.target.id as SectionId);
      },
      { rootMargin: '-15% 0px -70% 0px' },
    );
    SECTION_IDS.forEach((id) => {
      const el = this.doc.getElementById(id);
      if (el) observer.observe(el);
    });
    this.destroyRef.onDestroy(() => observer.disconnect());
  }

  /* ---------------- submit ---------------- */

  submit(): void {
    this.submitted.set(true);
    this.registered.set(false);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      setTimeout(() => {
        const first = this.host.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
        first?.scrollIntoView({ block: 'center' });
        first?.focus({ preventScroll: true });
      });
      return;
    }

    // TODO: call the API with this payload once the business creation endpoint is available.
    this.toBusinessPayload();
    this.registered.set(true);
  }

  private toBusinessPayload(): Omit<Business, 'id' | 'ownerId' | 'createdAt'> {
    const v = this.form.getRawValue();
    return {
      category: this.selectedCategory() ?? BUSINESS_CATEGORIES[0],
      name: v.name.trim(),
      slug: v.slug.trim(),
      description: v.description.trim() || null,
      location: this.location(),
      phone: this.phoneDisplay() || null,
      email: v.email.trim(),
      timezone: v.timezone,
      photoUrls: this.photos().map((p) => p.src),
      thumbnailUrl: this.coverPhoto()?.src ?? null,
      tags: this.tags(),
    };
  }
}
