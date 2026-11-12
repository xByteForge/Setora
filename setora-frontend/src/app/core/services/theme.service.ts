import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly currentTheme = signal<Theme>(this.getInitialTheme());

  constructor() {
    effect(() => {
      const theme = this.currentTheme();
      if (typeof document !== 'undefined') {
        document.body.classList.remove('light-theme', 'dark-theme');
        document.body.classList.add(`${theme}-theme`);
        localStorage.setItem('setora_theme', theme);
      }
    });

    if (typeof window !== 'undefined') {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (!localStorage.getItem('setora_theme')) {
          this.currentTheme.set(e.matches ? 'dark' : 'light');
        }
      });
    }
  }

  toggleTheme(): void {
    this.currentTheme.update(t => (t === 'light' ? 'dark' : 'light'));
  }

  private getInitialTheme(): Theme {
    if (typeof window === 'undefined') return 'light';
    const saved = localStorage.getItem('setora_theme') as Theme;
    if (saved) return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
