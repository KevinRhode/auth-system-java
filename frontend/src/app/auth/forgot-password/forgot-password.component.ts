import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './forgot-password.component.scss',
  template: `
    <div class="auth-card">
      @if (!sent()) {
        <h2>Forgot your password?</h2>
        <p class="subtitle">Enter your email and we'll send you a link to reset it.</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Email</label>
            <input
              type="email"
              formControlName="email"
              placeholder="you@example.com"
              [class.invalid]="isInvalid('email')"
            />
            @if (isInvalid('email')) {
              <span class="field-error">{{ getError('email') }}</span>
            }
          </div>

          @if (serverError()) {
            <p class="error">{{ serverError() }}</p>
          }

          <button class="btn btn-primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Sending...' : 'Send reset link' }}
          </button>
        </form>
      } @else {
        <div class="icon">📧</div>
        <h2>Check your email</h2>
        <p class="subtitle">
          If an account exists for <strong>{{ form.get('email')?.value }}</strong
          >, we've sent a password reset link. It expires in 1 hour.
        </p>
      }

      <div class="form-footer">
        <a routerLink="/login">Back to login</a>
      </div>
    </div>
  `,
})
export class ForgotPasswordComponent {
  form: FormGroup;
  serverError = signal('');
  loading = signal(false);
  sent = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.errors?.['required']) return 'This field is required';
    if (control?.errors?.['email']) return 'Enter a valid email address';
    return '';
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.serverError.set('');

    this.authService.forgotPassword(this.form.value.email).subscribe({
      next: () => this.sent.set(true),
      error: (err) => {
        const errorMsg = err.error?.error || '';

        if (err.status === 429 || errorMsg === 'RATE_LIMITED') {
          this.serverError.set('Too many attempts. Please wait and try again later.');
        } else {
          this.serverError.set('Something went wrong. Please try again.');
        }

        this.loading.set(false);
      },
    });
  }
}
