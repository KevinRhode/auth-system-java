import { Component } from '@angular/core';
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
      </div>

      <div class="nav-actions">
        <button class="btn-logout" (click)="logout()">Logout</button>
      </div>
    </nav>
  `
})
export class NavComponent {
  constructor(private authService: AuthService) {}

  logout() {
    this.authService.logout().subscribe();
  }
}