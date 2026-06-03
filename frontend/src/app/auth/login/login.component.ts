import { Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './login.component.scss',
  template: `
    <div class="auth-card">
      <h2>Welcome back</h2>
      <p class="subtitle">Sign in to your account</p>

      @if (verified()) {
        <div class="alert alert-success">✅ Email verified — you can now log in.</div>
      }

      @if (unverified()) {
        <div class="alert alert-warning">
          ⚠️ Please verify your email before logging in.
          <a routerLink="/verify-email-sent"
            [queryParams]="{ email: form.get('email')?.value }">
            Resend link
          </a>
        </div>
      }

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

        <div class="form-group">
          <label>Password</label>
          <input
            type="password"
            formControlName="password"
            placeholder="••••••••"
            [class.invalid]="isInvalid('password')"
          />
          @if (isInvalid('password')) {
            <span class="field-error">{{ getError('password') }}</span>
          }
        </div>

        @if (serverError()) {
          <p class="error">{{ serverError() }}</p>
        }

        <button class="btn btn-primary" type="submit" [disabled]="loading()">
          {{ loading() ? 'Signing in...' : 'Sign in' }}
        </button>

      </form>

      <div class="form-footer">
        Don't have an account? <a routerLink="/register">Register</a>
      </div>
    </div>
  `
})
export class LoginComponent implements OnInit {
  form: FormGroup;
  serverError = signal('');
  loading = signal(false);
  verified = signal(false);
  unverified = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.verified.set(
      this.route.snapshot.queryParamMap.get('verified') === 'true'
    );
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.errors?.['required']) return 'This field is required';
    if (control?.errors?.['email']) return 'Enter a valid email address';
    if (control?.errors?.['server']) return control.errors['server'];
    return '';
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.serverError.set('');
    this.unverified.set(false);

    this.authService.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: err => {
        const errorMsg = err.error?.error || '';

        if (errorMsg === 'EMAIL_NOT_VERIFIED') {
          this.unverified.set(true);
        } else if (errorMsg === 'VALIDATION_FAILED') {
          this.applyServerErrors(err.error.fields);
        } else if (errorMsg === 'Invalid credentials') {
          this.serverError.set('Invalid email or password');
        } else {
          this.serverError.set('Something went wrong. Please try again.');
        }

        this.loading.set(false);
      }
    });
  }

  private applyServerErrors(fields: Record<string, string>) {
    Object.entries(fields).forEach(([field, message]) => {
      this.form.get(field)?.setErrors({ server: message });
    });
  }
}