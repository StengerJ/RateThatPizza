import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Rating } from '../../core/models/rating.model';
import { AuthService } from '../../core/services/auth.service';
import { RatingsService } from '../../core/services/ratings.service';

@Component({
  selector: 'app-ratings-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './ratings-page.component.html',
  styleUrls: ['./ratings-page.component.css']
})
export class RatingsPage implements OnInit {
  private readonly ratingsService = inject(RatingsService);
  private readonly auth = inject(AuthService);

  readonly ratings = signal<Rating[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.ratingsService
      .listRatings()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (ratings) => this.ratings.set(ratings),
        error: () => this.ratings.set([])
      });
  }

  canCreate(): boolean {
    return this.auth.hasAnyRole(['CONTRIBUTOR', 'ADMIN']);
  }
}
