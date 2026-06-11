import { Component, OnInit, signal } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

function passwordStrength(control: AbstractControl): ValidationErrors | null {
  const value = control.value || '';
  const hasUpper = /[A-Z]/.test(value);
  const hasLower = /[a-z]/.test(value);
  const hasNumber = /\d/.test(value);
  if (!hasUpper || !hasLower || !hasNumber) {
    return { passwordStrength: true };
  }
  return null;
}

function passwordMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './reset-password.component.scss',
  template: `
    <div class="auth-card">
      @if (state() === 'missing') {
        <div class="icon">🔗</div>
        <h2>Invalid link</h2>
        <p class="subtitle">No reset token found. Use the link from your email.</p>
        <a routerLink="/forgot-password" class="btn btn-primary">Request a new link</a>
      }

      @if (state() === 'invalid') {
        <div class="icon">❌</div>
        <h2>Link expired</h2>
        <p class="subtitle">
          This password reset link is invalid or has expired. Reset links are valid for 1 hour and
          can only be used once.
        </p>
        <a routerLink="/forgot-password" class="btn btn-primary">Request a new link</a>
      }

      @if (state() === 'form') {
        <h2>Choose a new password</h2>
        <p class="subtitle">Your new password must be different from previous ones.</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>New password</label>
            <input
              type="password"
              formControlName="newPassword"
              placeholder="••••••••"
              [class.invalid]="isInvalid('newPassword')"
            />
            @if (isInvalid('newPassword')) {
              <span class="field-error">{{ getError('newPassword') }}</span>
            }

            <div class="password-rules">
              <span [class.met]="hasLength()">✓ At least 12 characters</span>
              <span [class.met]="hasUpper()">✓ One uppercase letter</span>
              <span [class.met]="hasLower()">✓ One lowercase letter</span>
              <span [class.met]="hasNumber()">✓ One number</span>
            </div>
          </div>

          <div class="form-group">
            <label>Confirm new password</label>
            <input
              type="password"
              formControlName="confirmPassword"
              placeholder="••••••••"
              [class.invalid]="showMismatch()"
            />
            @if (showMismatch()) {
              <span class="field-error">Passwords do not match</span>
            }
          </div>

          @if (serverError()) {
            <p class="error">{{ serverError() }}</p>
          }

          <button class="btn btn-primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Updating...' : 'Reset password' }}
          </button>
        </form>

        <div class="form-footer">
          <a routerLink="/login">Back to login</a>
        </div>
      }
    </div>
  `,
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  serverError = signal('');
  loading = signal(false);
  state = signal<'form' | 'missing' | 'invalid'>('form');

  private token = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
  ) {
    this.form = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(12), passwordStrength]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordMatch },
    );
  }

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.state.set('missing');
    }
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  showMismatch(): boolean {
    const confirm = this.form.get('confirmPassword');
    return !!(
      this.form.errors?.['passwordMismatch'] &&
      confirm &&
      (confirm.dirty || confirm.touched)
    );
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.errors?.['required']) return 'This field is required';
    if (control?.errors?.['minlength'])
      return `Minimum ${control.errors['minlength'].requiredLength} characters`;
    if (control?.errors?.['passwordStrength'])
      return 'Must contain uppercase, lowercase and a number';
    return '';
  }

  hasLength() {
    return (this.form.get('newPassword')?.value?.length || 0) >= 12;
  }
  hasUpper() {
    return /[A-Z]/.test(this.form.get('newPassword')?.value || '');
  }
  hasLower() {
    return /[a-z]/.test(this.form.get('newPassword')?.value || '');
  }
  hasNumber() {
    return /\d/.test(this.form.get('newPassword')?.value || '');
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.serverError.set('');

    this.authService.resetPassword(this.token, this.form.value.newPassword).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { reset: 'true' } }),
      error: (err) => {
        const errorMsg = err.error?.error || '';

        if (errorMsg === 'INVALID_RESET_TOKEN' || errorMsg === 'TOKEN_EXPIRED') {
          this.state.set('invalid');
        } else if (err.status === 429 || errorMsg === 'RATE_LIMITED') {
          this.serverError.set('Too many attempts. Please wait and try again later.');
        } else if (errorMsg === 'VALIDATION_FAILED' && err.error?.fields?.newPassword) {
          this.serverError.set(err.error.fields.newPassword);
        } else {
          this.serverError.set('Something went wrong. Please try again.');
        }

        this.loading.set(false);
      },
    });
  }
}
