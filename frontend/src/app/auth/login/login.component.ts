import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

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

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: err => {
        this.error = err.error?.message || 'Invalid credentials';
        this.loading = false;
      }
    });
  }
}