export interface CountryOption {
  code: string;
  flag: string;
  name: string;
}

export const COUNTRY_OPTIONS: CountryOption[] = [
  { code: '+40', flag: '🇷🇴', name: 'Romania' },
  { code: '+373', flag: '🇲🇩', name: 'Moldova' },
  { code: '+44', flag: '🇬🇧', name: 'UK' },
  { code: '+1', flag: '🇺🇸', name: 'USA / Canada' },
  { code: '+33', flag: '🇫🇷', name: 'France' },
  { code: '+49', flag: '🇩🇪', name: 'Germany' },
  { code: '+39', flag: '🇮🇹', name: 'Italy' },
  { code: '+34', flag: '🇪🇸', name: 'Spain' },
];