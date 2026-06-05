import { Component, OnInit, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminService } from '../../core/services/admin.service';
import { UserDto } from '../../core/services/auth.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [DatePipe],
  styleUrl: './users.component.scss',
  template: `
    <div class="users-page">

      <div class="page-header">
        <div>
          <h1>Users</h1>
          <p class="subtitle">{{ users().length }} total users</p>
        </div>
      </div>

      @if (loading()) {
        <div class="state-message">Loading users...</div>
      } @else if (users().length === 0) {
        <div class="state-message">No users found.</div>
      } @else {
        <div class="table-card">
          <table class="users-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Role</th>
                <th>Verified</th>
                <th>Joined</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (user of users(); track user.id) {
                <tr>
                  <td>
                    <div class="user-cell">
                      <div class="user-avatar">{{ getInitial(user) }}</div>
                      <div class="user-info">
                        <span class="user-name">{{ user.name }}</span>
                        <span class="user-email">{{ user.email }}</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <select class="role-select"
                      [value]="user.role"
                      (change)="updateRole(user.id, $event)">
                      <option value="USER">User</option>
                      <option value="ADMIN">Admin</option>
                      <option value="MODERATOR">Moderator</option>
                    </select>
                  </td>
                  <td>
                    <span class="badge" [class.verified]="user.emailVerified">
                      {{ user.emailVerified ? '✓ Verified' : '✗ Unverified' }}
                    </span>
                  </td>
                  <td class="date-cell">
                    {{ user.createdAt | date: 'MMM d, y' }}
                  </td>
                  <td>
                    <button class="btn-delete" (click)="deleteUser(user.id)">
                      Delete
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

    </div>
  `
})
export class UsersComponent implements OnInit {
  users = signal<UserDto[]>([]);
  loading = signal(true);

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading.set(true);
    this.adminService.getUsers().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  updateRole(id: string, event: Event) {
    const role = (event.target as HTMLSelectElement).value;
    this.adminService.updateRole(id, role).subscribe({
      next: updated => {
        this.users.update(list =>
          list.map(u => u.id === id ? updated : u)
        );
      }
    });
  }

  deleteUser(id: string) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    this.adminService.deleteUser(id).subscribe({
      next: () => this.users.update(list => list.filter(u => u.id !== id))
    });
  }

  getInitial(user: UserDto): string {
    return (user.name?.charAt(0) ?? user.email?.charAt(0) ?? '?').toUpperCase();
  }
}