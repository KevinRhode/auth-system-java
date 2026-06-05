import { Component, signal, HostListener, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  styleUrl: './nav.component.scss',
  template: `
    <nav class="navbar">
      <div class="nav-brand">
        <a routerLink="/dashboard">AuthSystem</a>
      </div>

      <div class="nav-links">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        @if (isAdmin()) {
          <a routerLink="/admin/users" routerLinkActive="active">Users</a>
          <a routerLink="/company" routerLinkActive="active">Company</a>
        }
      </div>

       <div class="nav-actions">
        <div class="avatar-wrapper">

          <button class="avatar-btn" (click)="toggleDropdown()">
            <div class="avatar">
              {{ initial() }}
            </div>
            <svg class="chevron" [class.open]="dropdownOpen()"
              xmlns="http://www.w3.org/2000/svg" width="14" height="14"
              viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>

          @if (dropdownOpen()) {
            <div class="dropdown">
              <div class="dropdown-header">
                <div class="avatar avatar-sm">{{ initial() }}</div>
                <div class="dropdown-user">
                  <span class="dropdown-name">{{ name() }}</span>
                  <span class="dropdown-email">{{ email() }}</span>
                </div>
              </div>

              <div class="dropdown-divider"></div>

              <a routerLink="/settings" class="dropdown-item" (click)="closeDropdown()">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15"
                  viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="3"/>
                  <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
                </svg>
                Settings
              </a>

              <div class="dropdown-divider"></div>

              <button class="dropdown-item danger" (click)="logout()">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15"
                  viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                Logout
              </button>
            </div>
          }

        </div>
      </div>
    </nav>
  `
})
export class NavComponent {
  private authService = inject(AuthService);

  dropdownOpen = signal(false);

  // computed signals — automatically update when currentUser changes
  isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN');
  name    = computed(() => this.authService.currentUser()?.name  ?? '');
  email   = computed(() => this.authService.currentUser()?.email ?? '');
  initial = computed(() => {
    const user = this.authService.currentUser();
    return (user?.name?.charAt(0) ?? user?.email?.charAt(0) ?? '?').toUpperCase();
  });

  toggleDropdown() { this.dropdownOpen.update(v => !v); }
  closeDropdown()  { this.dropdownOpen.set(false); }

  logout() {
    this.authService.logout().subscribe();
    this.closeDropdown();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.avatar-wrapper')) {
      this.dropdownOpen.set(false);
    }
  }
}