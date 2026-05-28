import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
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
          <a routerLink="/verify-email-sent" [queryParams]="{ email: email }">Resend link</a>
        </div>
      }

      <form (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label>Email</label>
          <input type="email" [(ngModel)]="email" name="email" placeholder="you@example.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input type="password" [(ngModel)]="password" name="password" placeholder="••••••••" required />
        </div>

        @if (error()) {
          <p class="error">{{ error() }}</p>
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
  email = '';
  password = '';

  error = signal('');
  loading = signal(false);
  verified = signal(false);
  unverified = signal(false);

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.verified.set(
      this.route.snapshot.queryParamMap.get('verified') === 'true'
    );
  }

  onSubmit() {
    this.loading.set(true);
    this.error.set('');
    this.unverified.set(false);

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: err => {
        const errorMsg = err.error?.error || err.error?.message || '';

        if (errorMsg === 'EMAIL_NOT_VERIFIED') {
          this.unverified.set(true);
        } else if (errorMsg === 'Invalid credentials') {
          this.error.set('Invalid email or password');
        } else {
          this.error.set('Something went wrong. Please try again.');
        }

        this.loading.set(false);
      }
    });
  }
}