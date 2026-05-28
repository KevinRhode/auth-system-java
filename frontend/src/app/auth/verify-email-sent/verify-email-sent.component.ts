import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-verify-email-sent',
  standalone: true,
  imports: [RouterLink],
  styleUrl: './verify-email-sent.component.scss',
  template: `
    <div class="auth-card">
      <div class="icon">📧</div>
      <h2>Check your email</h2>
      <p class="subtitle">
        We sent a verification link to <strong>{{ email }}</strong>.
        Click the link to activate your account.
      </p>
      <p class="note">Didn't get it? Check your spam folder or</p>
      <button class="btn btn-primary" (click)="resend()" [disabled]="resent">
        {{ resent ? 'Email sent!' : 'Resend verification email' }}
      </button>
      <div class="form-footer">
        <a routerLink="/login">Back to login</a>
      </div>
    </div>
  `
})
export class VerifyEmailSentComponent implements OnInit {
  email = '';
  resent = false;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    this.email = this.route.snapshot.queryParamMap.get('email') || '';
  }

  resend() {
    this.http.post(`${environment.apiUrl}/api/auth/resend-verification`,
      { email: this.email },
      { withCredentials: true }
    ).subscribe({
      next: () => this.resent = true,
      error: () => this.resent = true
    });
  }
}