import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './register.component.scss',
  template: `
    <div class="auth-card">
      <h2>Create an account</h2>
      <p class="subtitle">Sign up to get started</p>

      <form [formGroup]="form" (ngSubmit)="onSubmit()">

        <div class="form-group">
          <label>Full Name</label>
          <input
            type="text"
            formControlName="name"
            placeholder="John Doe"
            [class.invalid]="isInvalid('name')"
          />
          @if (isInvalid('name')) {
            <span class="field-error">{{ getError('name') }}</span>
          }
        </div>

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

          <div class="password-rules">
            <span [class.met]="hasLength()">✓ At least 12 characters</span>
            <span [class.met]="hasUpper()">✓ One uppercase letter</span>
            <span [class.met]="hasLower()">✓ One lowercase letter</span>
            <span [class.met]="hasNumber()">✓ One number</span>
          </div>
        </div>

        @if (serverError()) {
          <p class="error">{{ serverError() }}</p>
        }

        <button class="btn btn-primary" type="submit" [disabled]="loading()">
          {{ loading() ? 'Creating account...' : 'Register' }}
        </button>

      </form>

      <div class="form-footer">
        Already have an account? <a routerLink="/login">Sign in</a>
      </div>
    </div>
  `
})
export class RegisterComponent {
  form: FormGroup;
  serverError = signal('');
  loading = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(12), passwordStrength]]
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.errors?.['required'])        return 'This field is required';
    if (control?.errors?.['email'])           return 'Enter a valid email address';
    if (control?.errors?.['minlength'])       return `Minimum ${control.errors['minlength'].requiredLength} characters`;
    if (control?.errors?.['maxlength'])       return `Maximum ${control.errors['maxlength'].requiredLength} characters`;
    if (control?.errors?.['passwordStrength']) return 'Must contain uppercase, lowercase and a number';
    if (control?.errors?.['server'])          return control.errors['server'];
    return '';
  }

  hasLength() { return (this.form.get('password')?.value?.length || 0) >= 12; }
  hasUpper()  { return /[A-Z]/.test(this.form.get('password')?.value || ''); }
  hasLower()  { return /[a-z]/.test(this.form.get('password')?.value || ''); }
  hasNumber() { return /\d/.test(this.form.get('password')?.value || ''); }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.serverError.set('');

    this.authService.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/verify-email-sent'],
        { queryParams: { email: this.form.get('email')?.value } }),
      error: err => {
        const errorMsg = err.error?.error || '';
        if (errorMsg === 'VALIDATION_FAILED') {
          this.applyServerErrors(err.error.fields);
        } else {
          this.serverError.set(err.error?.error || 'Registration failed');
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