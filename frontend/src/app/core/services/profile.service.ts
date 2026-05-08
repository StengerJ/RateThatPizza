import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, shareReplay, tap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ContributorProfileSummary, UserProfile, UserProfileUpdateRequest } from '../models/profile.model';

const CONTRIBUTOR_CACHE_TTL_MS = 15 * 60 * 1000;

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;
  private contributorCache:
    | { expiresAt: number; request$: Observable<ContributorProfileSummary[]> }
    | null = null;

  getProfile(id: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/profiles/${encodeURIComponent(id)}`);
  }

  listContributors(): Observable<ContributorProfileSummary[]> {
    const now = Date.now();

    if (this.contributorCache && this.contributorCache.expiresAt > now) {
      return this.contributorCache.request$;
    }

    const request$ = this.http.get<ContributorProfileSummary[]>(`${this.apiUrl}/profiles/contributors`)
      .pipe(
        catchError((error) => {
          this.contributorCache = null;
          return throwError(() => error);
        }),
        shareReplay({ bufferSize: 1, refCount: false })
      );

    this.contributorCache = {
      expiresAt: now + CONTRIBUTOR_CACHE_TTL_MS,
      request$
    };

    return request$;
  }

  updateMyProfile(request: UserProfileUpdateRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/profiles/me`, request)
      .pipe(tap(() => this.contributorCache = null));
  }
}
