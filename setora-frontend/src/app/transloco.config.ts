import { provideTransloco, Translation } from '@ngneat/transloco';
import { isDevMode } from '@angular/core';
import { TranslocoHttpLoader } from './transloco-loader';

function getInitialLang(): string {
  if (typeof window === 'undefined') return 'ro';
  const saved = localStorage.getItem('setora_lang');
  if (saved) return saved;

  const browserLang = navigator.language.toLowerCase();
  return browserLang.startsWith('ro') ? 'ro' : 'en';
}

export const translocoConfig = provideTransloco({
  config: {
    availableLangs: ['ro', 'en'],
    defaultLang: getInitialLang(),
    reRenderOnLangChange: true,
    prodMode: !isDevMode(),
  },
  loader: TranslocoHttpLoader
});
