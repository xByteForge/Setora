export interface PageResult<T> {
    items: T[];
    total: number;
    page: number;      // incepe de la 1
    pageSize: number;
}