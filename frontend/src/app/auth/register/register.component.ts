import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  styleUrl: './register.component.scss',
  host: { class: 'auth-container' },
  template: `
    <div class="auth-card">
    <h2>Create an account</h2>
    <p class="subtitle">Sign up to get started</p>
    <form (ngSubmit)="onSubmit()">
      <div class="form-group">
        <label>Full Name</label>
        <input type="text" [(ngModel)]="name" name="name" placeholder="John Doe" required />
      </div>
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
        {{ loading ? 'Creating account...' : 'Register' }}
      </button>
    </form>
    <div class="form-footer">
      Already have an account? <a routerLink="/login">Sign in</a>
    </div>
  </div>
  `
})
export class RegisterComponent {
  name = '';
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
  this.loading = true;
  this.error = '';
  this.authService.register({
    name: this.name,
    email: this.email,
    password: this.password
  }).subscribe({
    next: () => this.router.navigate(['/verify-email-sent'],
      { queryParams: { email: this.email } }),
    error: err => {
      this.error = err.error?.message || 'Registration failed';
      this.loading = false;
    }
  });
}
}