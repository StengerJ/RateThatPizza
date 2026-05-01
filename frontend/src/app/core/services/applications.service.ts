import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ContributorApplication, ContributorApplicationRequest } from '../models/application.model';

@Injectable({
  providedIn: 'root'
})
export class ApplicationsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  submitApplication(request: ContributorApplicationRequest): Observable<ContributorApplication> {
    return this.http.post<ContributorApplication>(`${this.apiUrl}/applications`, request);
  }

  listApplications(): Observable<ContributorApplication[]> {
    return this.http.get<ContributorApplication[]>(`${this.apiUrl}/admin/applications`);
  }

  approveApplication(id: string): Observable<ContributorApplication> {
    return this.http.post<ContributorApplication>(
      `${this.apiUrl}/admin/applications/${encodeURIComponent(id)}/approve`,
      {}
    );
  }

  rejectApplication(id: string): Observable<ContributorApplication> {
    return this.http.post<ContributorApplication>(
      `${this.apiUrl}/admin/applications/${encodeURIComponent(id)}/reject`,
      {}
    );
  }
}
