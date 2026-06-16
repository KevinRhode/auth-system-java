import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-verify-email-error',
  standalone: true,
  imports: [RouterLink],
  styleUrl: './verify-email-error.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="auth-card">
      <div class="icon">❌</div>
      <h2>Verification failed</h2>
      <p class="subtitle">
        This link is invalid or has expired. Request a new one from the login page.
      </p>
      <a routerLink="/login" class="btn btn-primary">Back to login</a>
    </div>
  `,
})
export class VerifyEmailErrorComponent {}
