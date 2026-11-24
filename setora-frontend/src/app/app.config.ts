import {
  ApplicationConfig,
  PLATFORM_ID,
  provideBrowserGlobalErrorListeners,
  isDevMode,
  inject,
  provideAppInitializer,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { TranslocoHttpLoader } from './transloco-loader';
import { provideTransloco } from '@ngneat/transloco';
import { firstValueFrom } from 'rxjs';
import { provideApi } from './core/api/api.config';
import { AuthService } from './core/services/auth.service';
import { UserService } from './core/services/user.service';
import { isPlatformBrowser } from '@angular/common';

function getInitialLang(): string {
  if (typeof window === 'undefined') return 'ro';
  const saved = localStorage.getItem('setora_lang');
  if (saved === 'ro' || saved === 'en') return saved;

  return navigator.language.toLowerCase().startsWith('ro') ? 'ro' : 'en';
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(),
    provideHttpClient(withFetch()),
    provideTransloco({
      config: {
        availableLangs: ['ro', 'en'],
        defaultLang: getInitialLang(),
        reRenderOnLangChange: true,
        prodMode: !isDevMode(),
      },
      loader: TranslocoHttpLoader,
    }),
    provideApi(),
    provideAppInitializer(async () => {
      // Login-ul temporar rulează doar în browser: pe server nu există sesiune per utilizator.
      if (!isPlatformBrowser(inject(PLATFORM_ID))) return;

      const auth = inject(AuthService);
      const users = inject(UserService);

      try {
        const session = await auth.autoLogin();
        users.setCurrentUser(session.user);
      } catch (error) {
        console.error('Auto-login failed', error);
        return;
      }

      try {
        const user = users.currentUser();
        if (user) await firstValueFrom(users.loadAvatar(user.id));
      } catch (error) {
        console.error('Avatar load failed', error);
      }
    }),
  ],
};
