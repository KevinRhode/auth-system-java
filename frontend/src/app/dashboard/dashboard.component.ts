import {
  Component,
  computed,
  inject,
  OnInit,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { SessionService, SessionDto } from '../core/services/session.service';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="dashboard">
      <div class="dashboard-header">
        <h1>Dashboard</h1>
        <p class="subtitle">Manage your active sessions</p>
      </div>

      <div class="sessions-card">
        <div class="sessions-header">
          <h2>Active Sessions ({{ sessions().length }})</h2>
          <button class="btn-revoke-all" (click)="revokeAll()" [disabled]="sessions().length === 0">
            Revoke All
          </button>
        </div>

        @if (loading()) {
          <div class="state-message">Loading sessions...</div>
        } @else if (sessions().length === 0) {
          <div class="state-message">No active sessions found.</div>
        } @else {
          <ul class="session-list">
            @for (session of sessions(); track session.id) {
              <li class="session-item" [class.current-session]="session.id === currentSessionId()">
                <div class="session-info">
                  <div class="session-agent-row">
                    <span class="session-agent">{{ parseAgent(session.userAgent) }}</span>
                    @if (session.id === currentSessionId()) {
                      <span class="current-badge">Current</span>
                    }
                  </div>
                  <span class="session-meta">
                    Started {{ session.createdAt | date: 'MMM d, y, h:mm a' }} · Expires
                    {{ session.expiresAt | date: 'MMM d, y' }}
                  </span>
                </div>
                @if (session.id !== currentSessionId()) {
                  <button class="btn-revoke" (click)="revoke(session.id)">Revoke</button>
                }
              </li>
            }
          </ul>
        }
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private sessionService = inject(SessionService);
  authService = inject(AuthService);

  currentSessionId = computed(() => this.authService.currentSessionId());
  sessions = signal<SessionDto[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.loadSessions();
  }

  loadSessions() {
    this.loading.set(true);
    this.sessionService.getSessions().subscribe({
      next: (data) => {
        this.sessions.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  revoke(id: string) {
    this.sessionService.revokeSession(id).subscribe({
      next: () => this.sessions.update((s) => s.filter((s) => s.id !== id)),
    });
  }

  revokeAll() {
    this.sessionService.revokeAllSessions().subscribe({
      next: () => {
        this.sessions.set([]);
        this.authService.logout().subscribe();
      },
    });
  }

  parseAgent(userAgent: string): string {
    if (!userAgent) return '🖥️ Unknown device';
    if (userAgent.includes('Edg')) return 'Edge';
    if (userAgent.includes('Chrome')) return 'Chrome';
    if (userAgent.includes('Firefox')) return 'Firefox';
    if (userAgent.includes('Safari')) return 'Safari';
    return '🖥️ Unknown browser';
  }
}
