import { ActivatedRouteSnapshot, DetachedRouteHandle } from '@angular/router';

import { TimedRouteReuseStrategy } from './timed-route-reuse.strategy';

describe('TimedRouteReuseStrategy', () => {
  it('should reuse routes with a valid cache TTL', () => {
    const strategy = new TimedRouteReuseStrategy();
    const route = routeSnapshot('contributors', 900000);
    const handle = {} as DetachedRouteHandle;
    spyOn(Date, 'now').and.returnValue(1000);

    expect(strategy.shouldDetach(route)).toBeTrue();

    strategy.store(route, handle);

    expect(strategy.shouldAttach(route)).toBeTrue();
    expect(strategy.retrieve(route)).toBe(handle);
  });

  it('should expire cached routes after their TTL', () => {
    const strategy = new TimedRouteReuseStrategy();
    const route = routeSnapshot('contributors', 1000);
    const handle = {} as DetachedRouteHandle;
    const nowSpy = spyOn(Date, 'now').and.returnValue(1000);

    strategy.store(route, handle);
    nowSpy.and.returnValue(2000);

    expect(strategy.shouldAttach(route)).toBeFalse();
    expect(strategy.retrieve(route)).toBeNull();
  });

  it('should not detach routes without cache data', () => {
    const strategy = new TimedRouteReuseStrategy();

    expect(strategy.shouldDetach(routeSnapshot('ratings'))).toBeFalse();
  });
});

function routeSnapshot(path: string, cacheTtlMs?: number): ActivatedRouteSnapshot {
  return {
    data: cacheTtlMs === undefined ? {} : { cacheTtlMs },
    outlet: 'primary',
    routeConfig: { path }
  } as ActivatedRouteSnapshot;
}
