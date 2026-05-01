import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class Navbar {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  canContribute(): boolean {
    return this.auth.hasAnyRole(['CONTRIBUTOR', 'ADMIN']);
  }

  canAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/');
  }
}
