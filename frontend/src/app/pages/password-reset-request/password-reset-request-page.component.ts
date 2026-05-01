import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-password-reset-request-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './password-reset-request-page.component.html',
  styleUrls: ['./password-reset-request-page.component.css']
})
export class PasswordResetRequestPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly submitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.auth.requestPasswordReset(this.form.controls.email.value.trim()).subscribe({
      next: () => {
        this.successMessage.set('If an account exists for that email, a reset link will be sent.');
        this.form.reset();
        this.submitting.set(false);
      },
      error: () => {
        this.errorMessage.set('Password reset could not be requested. Please try again later.');
        this.submitting.set(false);
      }
    });
  }
}
