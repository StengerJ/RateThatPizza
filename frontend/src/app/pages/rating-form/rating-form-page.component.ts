import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { RatingsService } from '../../core/services/ratings.service';

@Component({
  selector: 'app-rating-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './rating-form-page.component.html',
  styleUrls: ['./rating-form-page.component.css']
})
export class RatingFormPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly ratingsService = inject(RatingsService);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    restaurantName: ['', [Validators.required, Validators.minLength(2)]],
    sauce: ['', [Validators.required]],
    toppings: ['', [Validators.required]],
    crust: ['', [Validators.required]],
    overallRating: [8, [Validators.required, Validators.min(1), Validators.max(10)]],
    comments: ['', [Validators.required, Validators.minLength(5)]]
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal('');

  submit(): void {
    this.errorMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    this.submitting.set(true);
    this.ratingsService
      .createRating({
        restaurantName: value.restaurantName.trim(),
        sauce: value.sauce.trim(),
        toppings: value.toppings.trim(),
        crust: value.crust.trim(),
        overallRating: value.overallRating,
        comments: value.comments.trim()
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          void this.router.navigateByUrl('/ratings');
        },
        error: () => {
          this.errorMessage.set('Rating could not be saved. Please try again later.');
          this.submitting.set(false);
        }
      });
  }
}
