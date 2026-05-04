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
  readonly errorMessage = signal('');
  readonly processingIds = signal<Set<string>>(new Set());

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

  canManage(rating: Rating): boolean {
    const user = this.auth.currentUser();
    return Boolean(user && (user.role === 'ADMIN' || user.id === rating.creatorId));
  }

  showActionsColumn(): boolean {
    return this.auth.isLoggedIn();
  }

  isProcessing(id?: string): boolean {
    return id ? this.processingIds().has(id) : false;
  }

  removeRating(rating: Rating): void {
    const ratingId = rating.id;

    if (!ratingId || !confirm(`Remove ${rating.restaurantName}?`)) {
      return;
    }

    this.errorMessage.set('');
    this.setProcessing(ratingId, true);

    this.ratingsService.deleteRating(ratingId).subscribe({
      next: () => {
        this.ratings.update((ratings) =>
          ratings.filter((currentRating) => currentRating.id !== ratingId)
        );
        this.setProcessing(ratingId, false);
      },
      error: () => {
        this.errorMessage.set('Rating could not be removed.');
        this.setProcessing(ratingId, false);
      }
    });
  }

  private setProcessing(id: string, processing: boolean): void {
    const nextIds = new Set(this.processingIds());

    if (processing) {
      nextIds.add(id);
    } else {
      nextIds.delete(id);
    }

    this.processingIds.set(nextIds);
  }
}
