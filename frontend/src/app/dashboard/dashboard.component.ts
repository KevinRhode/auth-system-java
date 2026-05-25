import { Component } from '@angular/core';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div>
      <h2>Dashboard</h2>
      <p>You are logged in.</p>
      <button (click)="logout()">Logout</button>
    </div>
  `
})
export class DashboardComponent {
  constructor(private authService: AuthService) {}

  logout() {
    this.authService.logout().subscribe();
  }
}