import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AdminContributor } from '../models/admin-contributor.model';
import { AdminUser } from '../models/admin-user.model';
import { UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  listContributors(): Observable<AdminContributor[]> {
    return this.http.get<AdminContributor[]>(`${this.apiUrl}/admin/contributors`);
  }

  disableContributor(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/contributors/${encodeURIComponent(id)}`);
  }

  listUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/admin/users`);
  }

  updateUserRole(id: string, role: UserRole): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.apiUrl}/admin/users/${encodeURIComponent(id)}/role`, {
      role
    });
  }
}
