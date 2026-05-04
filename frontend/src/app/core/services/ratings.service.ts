import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Rating, RatingCreateRequest } from '../models/rating.model';

@Injectable({
  providedIn: 'root'
})
export class RatingsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  listRatings(): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.apiUrl}/ratings`);
  }

  getRating(id: string): Observable<Rating> {
    return this.http.get<Rating>(`${this.apiUrl}/ratings/${encodeURIComponent(id)}`);
  }

  createRating(request: RatingCreateRequest): Observable<Rating> {
    return this.http.post<Rating>(`${this.apiUrl}/ratings`, request);
  }

  updateRating(id: string, request: RatingCreateRequest): Observable<Rating> {
    return this.http.put<Rating>(`${this.apiUrl}/ratings/${encodeURIComponent(id)}`, request);
  }

  deleteRating(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/ratings/${encodeURIComponent(id)}`);
  }
}
