import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  template: `
    <div class="auth-container">
      <h2>Register</h2>
      <form (ngSubmit)="onSubmit()">
        <input
          type="text"
          [(ngModel)]="name"
          name="name"
          placeholder="Full Name"
          required
        />
        <input
          type="email"
          [(ngModel)]="email"
          name="email"
          placeholder="Email"
          required
        />
        <input
          type="password"
          [(ngModel)]="password"
          name="password"
          placeholder="Password"
          required
        />
        <p *ngIf="error" class="error">{{ error }}</p>
        <button type="submit" [disabled]="loading">
          {{ loading ? 'Creating account...' : 'Register' }}
        </button>
      </form>
      <p>Already have an account? <a routerLink="/login">Login</a></p>
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
    this.authService.register({ name: this.name, email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: err => {
        this.error = err.error?.message || 'Registration failed';
        this.loading = false;
      }
    });
  }
}