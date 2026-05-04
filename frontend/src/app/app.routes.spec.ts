import { routes } from './app.routes';
import { adminGuard, contributorGuard } from './core/guards/role.guard';

describe('app routes', () => {
  it('should expose public pages', () => {
    const paths = routes.map((route) => route.path);

    expect(paths).toContain('');
    expect(paths).toContain('ratings');
    expect(paths).toContain('ratings/:id/edit');
    expect(paths).toContain('apply');
    expect(paths).toContain('about-me');
    expect(paths).toContain('blog');
    expect(paths).toContain('blog/:slug');
    expect(paths).toContain('blog/:slug/edit');
    expect(paths).toContain('login');
  });

  it('should protect contributor and admin routes', () => {
    const newRatingRoute = routes.find((route) => route.path === 'ratings/new');
    const editRatingRoute = routes.find((route) => route.path === 'ratings/:id/edit');
    const newBlogPostRoute = routes.find((route) => route.path === 'blog/new');
    const editBlogPostRoute = routes.find((route) => route.path === 'blog/:slug/edit');
    const adminRoute = routes.find((route) => route.path === 'admin/applications');

    expect(newRatingRoute?.canActivate).toContain(contributorGuard);
    expect(editRatingRoute?.canActivate).toContain(contributorGuard);
    expect(newBlogPostRoute?.canActivate).toContain(contributorGuard);
    expect(editBlogPostRoute?.canActivate).toContain(contributorGuard);
    expect(adminRoute?.canActivate).toContain(adminGuard);
  });
});
