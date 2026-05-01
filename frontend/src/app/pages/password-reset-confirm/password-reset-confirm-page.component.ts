import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-password-reset-confirm-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './password-reset-confirm-page.component.html',
  styleUrls: ['./password-reset-confirm-page.component.css']
})
export class PasswordResetConfirmPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.group({
    token: [this.route.snapshot.queryParamMap.get('token') ?? '', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

  readonly submitted = signal(false);
  readonly submitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  passwordsMatch(): boolean {
    const value = this.form.getRawValue();
    return value.password === value.confirmPassword;
  }

  submit(): void {
    this.submitted.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.form.invalid || !this.passwordsMatch()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    this.submitting.set(true);
    this.auth.confirmPasswordReset(value.token.trim(), value.password).subscribe({
      next: () => {
        this.successMessage.set('Password updated. You can now log in.');
        this.form.reset();
        this.submitting.set(false);
        this.submitted.set(false);
      },
      error: () => {
        this.errorMessage.set('Password could not be updated. The token may be invalid or expired.');
        this.submitting.set(false);
      }
    });
  }
}
