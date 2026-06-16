import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [RouterLink],
  styleUrl: './verify-email.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="auth-card">
      @if (state === 'loading') {
        <div class="icon">⏳</div>
        <h2>Verifying your email...</h2>
        <p class="subtitle">Please wait a moment.</p>
      }

      @if (state === 'success') {
        <div class="icon">✅</div>
        <h2>Email verified!</h2>
        <p class="subtitle">Your account is now active. You can log in.</p>
        <a routerLink="/login" class="btn btn-primary">Go to login</a>
      }

      @if (state === 'error') {
        <div class="icon">❌</div>
        <h2>Verification failed</h2>
        <p class="subtitle">{{ errorMessage }}</p>
        <a routerLink="/login" class="btn btn-primary">Back to login</a>
      }

      @if (state === 'missing') {
        <div class="icon">🔗</div>
        <h2>Invalid link</h2>
        <p class="subtitle">No verification token found. Use the link from your email.</p>
        <a routerLink="/login" class="btn btn-primary">Back to login</a>
      }
    </div>
  `,
})
export class VerifyEmailComponent implements OnInit {
  state: 'loading' | 'success' | 'error' | 'missing' = 'loading';
  errorMessage = 'This link is invalid or has expired.';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.state = 'missing';
      return;
    }

    this.http
      .get(`${environment.apiUrl}/api/auth/verify-email`, {
        params: { token },
        withCredentials: true,
      })
      .subscribe({
        next: () => {
          this.state = 'success';
          // auto redirect to login after 3 seconds
          setTimeout(
            () => this.router.navigate(['/login'], { queryParams: { verified: 'true' } }),
            3000,
          );
        },
        error: (err) => {
          this.state = 'error';
          this.errorMessage = err.error?.error || 'This link is invalid or has expired.';
        },
      });
  }
}
