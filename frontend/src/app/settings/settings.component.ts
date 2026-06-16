import {
  Component,
  computed,
  inject,
  OnInit,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { ConfirmModalComponent } from '../shared/confirm-modal/confirm-modal.component';
import { environment } from '../../environments/environment';
import { UserSettingsDto } from '../core/services/settings.service';

interface SettingsSection {
  id: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [ConfirmModalComponent],
  styleUrl: './settings.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="settings-layout">
      <aside class="settings-nav">
        <h2>Settings</h2>
        <nav>
          @for (section of sections; track section.id) {
            <button
              class="nav-item"
              [class.active]="activeSection() === section.id"
              (click)="activeSection.set(section.id)"
            >
              <span class="nav-icon">{{ section.icon }}</span>
              {{ section.label }}
            </button>
          }
        </nav>
      </aside>

      <main class="settings-content">
        @if (activeSection() === 'account') {
          <section class="settings-section">
            <h3>Account</h3>
            <p class="section-desc">Manage your account details.</p>
            <div class="setting-row">
              <span class="setting-label">User ID</span>
              <span class="setting-value">{{ settings()?.id ?? '—' }}</span>
            </div>
          </section>
        }

        @if (activeSection() === 'company') {
          <section class="settings-section">
            <h3>Company</h3>
            <p class="section-desc">Manage your company membership.</p>

            @if (inCompany()) {
              <div class="setting-row">
                <span class="setting-label">Company</span>
                <span class="setting-value">{{ companyName() }}</span>
                <!--                <span class="setting-value">{{ settings()!.companyId }}</span>-->
              </div>
              <div class="danger-zone">
                <h4>Danger zone</h4>
                <div class="danger-row">
                  <div>
                    <strong>Leave company</strong>
                    <p>You will lose access immediately. An admin must re-invite you to rejoin.</p>
                  </div>
                  <button class="btn btn-danger" (click)="showLeaveModal.set(true)">
                    Leave company
                  </button>
                </div>
              </div>
            } @else {
              <p class="empty-state">You are not a member of any company.</p>
            }
          </section>
        }
      </main>
    </div>

    <app-confirm-modal
      [open]="showLeaveModal()"
      title="Leave company"
      [message]="
        'You will lose access to company #' +
        companyName() +
        ' immediately. An admin must re-invite you to rejoin.'
      "
      confirmLabel="Leave company"
      variant="danger"
      [busy]="leaving()"
      (confirmed)="leaveCompany()"
      (cancelled)="showLeaveModal.set(false)"
    />
  `,
})
export class SettingsComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  readonly sections: SettingsSection[] = [
    { id: 'account', label: 'Account', icon: '👤' },
    { id: 'company', label: 'Company', icon: '🏢' },
    // Add new sections here — nav updates automatically
  ];

  activeSection = signal<string>('account');
  settings = signal<UserSettingsDto | null>(null);
  showLeaveModal = signal(false);
  leaving = signal(false);
  companyName = computed(() => this.settings()?.company?.name ?? null);
  inCompany = computed(() => this.settings()?.company?.id != null);

  ngOnInit(): void {
    this.http
      .get<UserSettingsDto>(`${environment.apiUrl}/api/user/settings`, { withCredentials: true })
      .subscribe({ next: (s) => this.settings.set(s) });
  }

  leaveCompany(): void {
    this.leaving.set(true);
    this.http
      .delete(`${environment.apiUrl}/api/company/me/leave`, { withCredentials: true })
      .subscribe({
        next: () => {
          this.settings.update((s) => (s ? { ...s, companyId: null } : s));
          this.showLeaveModal.set(false);
          this.leaving.set(false);
        },
        error: () => this.leaving.set(false),
      });
  }
}
