import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  styleUrl: './login.component.scss',
  host: { class: 'auth-container' },
  template: `
    <div class="auth-card">
    <h2>Welcome back</h2>
    <p class="subtitle">Sign in to your account</p>
    @if (verified) {
      <div class="alert alert-success">
        ✅ Email verified — you can now log in.
      </div>
    }
    @if (unverified) {
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

      <p *ngIf="error" class="error">{{ error }}</p>

      <button class="btn btn-primary" type="submit" [disabled]="loading">
        {{ loading ? 'Signing in...' : 'Sign in' }}
      </button>
    </form>

    <div class="form-footer">
      Don't have an account? <a routerLink="/register">Register</a>
    </div>
  </div>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;
  verified = false;
  unverified = false;

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {
  }
  ngOnInit() {
    this.verified = this.route.snapshot.queryParamMap.get('verified') === 'true';
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: err => {
        if (err.error?.message === 'EMAIL_NOT_VERIFIED') {
          this.unverified = true;
        } else {
          this.error = err.error?.message || 'Invalid credentials';
        }
          this.loading = false;
      }
    });
  }
}