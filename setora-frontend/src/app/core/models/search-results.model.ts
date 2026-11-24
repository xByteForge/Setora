export type SearchCategory = 'pages' | 'businesses' | 'services';

export interface SearchResult {
  id: string;
  category: SearchCategory;
  title: string;
  subtitle?: string;
  url: string;
}

export interface SearchGroup {
  category: SearchCategory;
  labelKey: string;
  items: SearchResult[];
}
