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
  private static readonly resetSentMessage =
    'Password reset sent, please allow up to five minutes to receive your reset link.';

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly submitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly requestSent = signal(false);

  submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');
    this.requestSent.set(false);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.auth.requestPasswordReset(this.form.controls.email.value.trim()).subscribe({
      next: () => {
        this.showResetSentMessage();
      },
      error: () => {
        this.showResetSentMessage();
      }
    });
  }

  retry(): void {
    this.successMessage.set('');
    this.errorMessage.set('');
    this.requestSent.set(false);
  }

  private showResetSentMessage(): void {
    this.successMessage.set(PasswordResetRequestPage.resetSentMessage);
    this.requestSent.set(true);
    this.submitting.set(false);
  }
}
