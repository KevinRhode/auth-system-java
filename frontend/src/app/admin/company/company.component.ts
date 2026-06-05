import { Component, OnInit, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CompanyService, CompanyDto, CompanyMemberDto } from '../../core/services/company.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-company',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  styleUrl: './company.component.scss',
  template: `
    <div class="company-page">

      @if (loading()) {
        <div class="state-message">Loading company...</div>
      } @else if (!company()) {
        <div class="empty-state">
          <h2>No company yet</h2>
          <p>Create an organization to manage your team.</p>
          <a routerLink="/company/create" class="btn btn-primary">
            Create Company
          </a>
        </div>
      } @else {
        <div class="page-header">
          <div>
            <h1>{{ company()!.name }}</h1>
            <p class="subtitle">{{ company()!.slug }} · {{ company()!.members.length }} members</p>
          </div>
        </div>

        <!-- Invite Member -->
        @if (isAdminOrOwner()) {
          <div class="invite-card">
            <h2>Invite a member</h2>
            <form [formGroup]="inviteForm" (ngSubmit)="invite()" class="invite-form">
              <input
                type="email"
                formControlName="email"
                placeholder="colleague@example.com"
              />
              <select formControlName="role">
                <option value="MEMBER">Member</option>
                <option value="ADMIN">Admin</option>
              </select>
              <button class="btn btn-primary" type="submit" [disabled]="inviting()">
                {{ inviting() ? 'Inviting...' : 'Invite' }}
              </button>
            </form>
            @if (inviteError()) {
              <p class="error">{{ inviteError() }}</p>
            }
          </div>
        }

        <!-- Members Table -->
        <div class="table-card">
          <h2>Members</h2>
          <table class="members-table">
            <thead>
              <tr>
                <th>Member</th>
                <th>Role</th>
                <th>Joined</th>
                @if (isAdminOrOwner()) { <th>Actions</th> }
              </tr>
            </thead>
            <tbody>
              @for (member of company()!.members; track member.id) {
                <tr>
                  <td>
                    <div class="user-cell">
                      <div class="user-avatar">{{ getInitial(member) }}</div>
                      <div class="user-info">
                        <span class="user-name">{{ member.name }}</span>
                        <span class="user-email">{{ member.email }}</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    @if (isAdminOrOwner() && member.role !== 'OWNER') {
                      <select class="role-select"
                        [value]="member.role"
                        (change)="updateRole(member.id, $event)">
                        <option value="MEMBER">Member</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                    } @else {
                      <span class="badge" [class]="member.role.toLowerCase()">
                        {{ member.role }}
                      </span>
                    }
                  </td>
                  <td class="date-cell">
                    {{ member.joinedAt | date: 'MMM d, y' }}
                  </td>
                  @if (isAdminOrOwner()) {
                    <td>
                      @if (member.role !== 'OWNER') {
                        <button class="btn-remove"
                          (click)="removeMember(member.id)">
                          Remove
                        </button>
                      }
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- Leave Company -->
        @if (myRole() !== 'OWNER') {
          <div class="danger-zone">
            <h3>Danger Zone</h3>
            <button class="btn-danger" (click)="leave()">Leave Company</button>
          </div>
        }
      }

    </div>
  `
})
export class CompanyComponent implements OnInit {
  company = signal<CompanyDto | null>(null);
  loading = signal(true);
  inviting = signal(false);
  inviteError = signal('');
  inviteForm: FormGroup;

  myRole = computed(() => {
    const userId = this.authService.currentUser()?.id;
    return this.company()?.members?.find(m => m.userId === userId)?.role ?? '';
  });

  isAdminOrOwner = computed(() =>
    this.myRole() === 'OWNER' || this.myRole() === 'ADMIN'
  );

  constructor(
    private companyService: CompanyService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['MEMBER']
    });
  }

  ngOnInit() {
    this.companyService.getMyCompany().subscribe({
      next: company => {
        this.company.set(company);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  invite() {
    if (this.inviteForm.invalid) return;
    this.inviting.set(true);
    this.inviteError.set('');

    const { email, role } = this.inviteForm.value;

    this.companyService.inviteMember(this.company()!.id, email, role).subscribe({
      next: member => {
        this.company.update(c => c
          ? { ...c, members: [...(c.members || []), member] }
          : c
        );
        this.inviteForm.reset({ role: 'MEMBER' });
        this.inviting.set(false);
      },
      error: err => {
        this.inviteError.set(err.error?.error || 'Failed to invite member');
        this.inviting.set(false);
      }
    });
  }

  updateRole(memberId: string, event: Event) {
    const role = (event.target as HTMLSelectElement).value;
    this.companyService.updateMemberRole(this.company()!.id, memberId, role).subscribe({
      next: updated => {
        this.company.update(c => c
          ? { ...c, members: c.members.map(m => m.id === memberId ? updated : m) }
          : c
        );
      }
    });
  }

  removeMember(memberId: string) {
    if (!confirm('Remove this member?')) return;
    this.companyService.removeMember(this.company()!.id, memberId).subscribe({
      next: () => {
        this.company.update(c => c
          ? { ...c, members: c.members.filter(m => m.id !== memberId) }
          : c
        );
      }
    });
  }

  leave() {
    if (!confirm('Are you sure you want to leave this company?')) return;
    this.companyService.leaveCompany().subscribe({
      next: () => this.company.set(null)
    });
  }

  getInitial(member: CompanyMemberDto): string {
    return (member.name?.charAt(0) ?? member.email?.charAt(0) ?? '?').toUpperCase();
  }
}