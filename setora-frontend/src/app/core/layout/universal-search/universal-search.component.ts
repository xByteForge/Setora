import {
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    HostListener,
    computed,
    inject,
    signal,
    viewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoDirective } from '@ngneat/transloco';
import { LucideSearch } from '@lucide/angular';
import { SearchService } from '../../services/search.service';
import { SearchResult } from '../../models/search-results.model';

let nextId = 0;

@Component({
    selector: 'app-universal-search',
    standalone: true,
    imports: [TranslocoDirective, LucideSearch],
    templateUrl: './universal-search.component.html',
    styleUrl: './universal-search.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UniversalSearchComponent {
    private readonly searchService = inject(SearchService);
    private readonly router = inject(Router);
    private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

    readonly panelId = `search-panel-${nextId++}`;
    readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('q');

    readonly query = signal('');
    readonly searchOpen = signal(false);
    readonly activeIndex = signal(0);
    readonly groups = computed(() => this.searchService.search(this.query()));
    readonly flat = computed(() => this.groups().flatMap((g) => g.items));

    onQueryInput(value: string): void {
        this.query.set(value);
        this.activeIndex.set(0);
        this.searchOpen.set(value.trim().length > 0);
    }

    onSearchKeydown(e: KeyboardEvent): void {
        const n = this.flat().length;

        if (e.key === 'ArrowDown' && n > 0) {
            e.preventDefault();
            this.activeIndex.update((i) => (i + 1) % n);
        } else if (e.key === 'ArrowUp' && n > 0) {
            e.preventDefault();
            this.activeIndex.update((i) => (i - 1 + n) % n);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            const item = this.flat()[this.activeIndex()];
            if (item) this.openResult(item);
            else if (this.query().trim()) this.seeAll();
        }
    }

    openResult(item: SearchResult): void {
        this.router.navigateByUrl(item.url);
        this.closeSearch();
    }

    seeAll(): void {
        const q = this.query().trim();
        if (!q) return;
        this.router.navigate(['/discover'], { queryParams: { q } });
        this.closeSearch();
    }

    private closeSearch(): void {
        this.searchOpen.set(false);
        this.query.set('');
        this.searchInput()?.nativeElement.blur();
    }

    @HostListener('document:click', ['$event'])
    onDocClick(e: MouseEvent): void {
        if (!this.host.nativeElement.contains(e.target as Node)) {
            this.searchOpen.set(false);
        }
    }

    @HostListener('document:keydown.escape')
    onEsc(): void {
        if (this.searchOpen()) this.closeSearch();
    }

    // "/" sau Ctrl/Cmd+K focusează search-ul
    @HostListener('document:keydown', ['$event'])
    onGlobalKey(e: KeyboardEvent): void {
        const input = this.searchInput()?.nativeElement;
        // ignoră instanța ascunsă (cea din topbar pe mobil, cea din pagină pe desktop)
        if (!input || input.offsetParent === null) return;

        const target = e.target as HTMLElement;
        const typing = /^(input|textarea|select)$/i.test(target.tagName) || target.isContentEditable;

        const isCmdK = (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k';
        const isSlash = e.key === '/' && !typing;

        if (isCmdK || isSlash) {
            e.preventDefault();
            input.focus();
        }
    }
}