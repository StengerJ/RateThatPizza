import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ContributorProfileSummary, UserProfile, UserProfileUpdateRequest } from '../models/profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  getProfile(id: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/profiles/${encodeURIComponent(id)}`);
  }

  listContributors(): Observable<ContributorProfileSummary[]> {
    return this.http.get<ContributorProfileSummary[]>(`${this.apiUrl}/profiles/contributors`);
  }

  updateMyProfile(request: UserProfileUpdateRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/profiles/me`, request);
  }
}
