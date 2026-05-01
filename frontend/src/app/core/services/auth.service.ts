import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, AuthSession, LoginRequest, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;
  private readonly storageKey = 'pghPizzaSession';
  private readonly sessionSignal = signal<AuthSession | null>(this.readSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly currentUser = computed(() => this.sessionSignal()?.user ?? null);
  readonly isLoggedIn = computed(() => Boolean(this.sessionSignal()?.token));

  token(): string | null {
    return this.sessionSignal()?.token ?? null;
  }

  login(credentials: LoginRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      map((response) => ({
        token: response.token,
        user: response.user
      })),
      tap((session) => this.persistSession(session))
    );
  }

  logout(): void {
    this.sessionSignal.set(null);
    localStorage.removeItem(this.storageKey);
  }

  hasAnyRole(roles: readonly UserRole[]): boolean {
    const role = this.currentUser()?.role;
    return role ? roles.includes(role) : false;
  }

  requestPasswordReset(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/password-reset/request`, { email });
  }

  confirmPasswordReset(token: string, password: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/password-reset/confirm`, { token, password });
  }

  private persistSession(session: AuthSession): void {
    this.sessionSignal.set(session);
    localStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  private readSession(): AuthSession | null {
    const rawSession = localStorage.getItem(this.storageKey);

    if (!rawSession) {
      return null;
    }

    try {
      const session = JSON.parse(rawSession) as AuthSession;
      return session.token && session.user ? session : null;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
