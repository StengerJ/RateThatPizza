import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  DetachedRouteHandle,
  PRIMARY_OUTLET,
  RouteReuseStrategy
} from '@angular/router';

interface CachedRoute {
  handle: DetachedRouteHandle;
  storedAt: number;
  ttlMs: number;
}

@Injectable()
export class TimedRouteReuseStrategy implements RouteReuseStrategy {
  private readonly cache = new Map<string, CachedRoute>();

  shouldDetach(route: ActivatedRouteSnapshot): boolean {
    return this.cacheTtlMs(route) > 0 && Boolean(route.routeConfig);
  }

  store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle | null): void {
    const key = this.cacheKey(route);

    if (!key) {
      return;
    }

    if (!handle) {
      this.cache.delete(key);
      return;
    }

    this.cache.set(key, {
      handle,
      storedAt: Date.now(),
      ttlMs: this.cacheTtlMs(route)
    });
  }

  shouldAttach(route: ActivatedRouteSnapshot): boolean {
    return Boolean(this.validCachedRoute(route));
  }

  retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle | null {
    return this.validCachedRoute(route)?.handle ?? null;
  }

  shouldReuseRoute(future: ActivatedRouteSnapshot, current: ActivatedRouteSnapshot): boolean {
    return future.routeConfig === current.routeConfig;
  }

  private validCachedRoute(route: ActivatedRouteSnapshot): CachedRoute | null {
    const key = this.cacheKey(route);

    if (!key) {
      return null;
    }

    const cachedRoute = this.cache.get(key);
    if (!cachedRoute) {
      return null;
    }

    if (Date.now() - cachedRoute.storedAt >= cachedRoute.ttlMs) {
      this.cache.delete(key);
      return null;
    }

    return cachedRoute;
  }

  private cacheKey(route: ActivatedRouteSnapshot): string | null {
    const path = route.routeConfig?.path;

    if (path === undefined) {
      return null;
    }

    return `${route.outlet || PRIMARY_OUTLET}:${path}`;
  }

  private cacheTtlMs(route: ActivatedRouteSnapshot): number {
    const ttlMs = route.data['cacheTtlMs'];
    return typeof ttlMs === 'number' ? ttlMs : 0;
  }
}
