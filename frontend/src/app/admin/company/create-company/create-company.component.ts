import { Component, signal, ChangeDetectionStrategy } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CompanyService } from '../../../core/services/company.service';

@Component({
  selector: 'app-create-company',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './create-company.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="page">
      <div class="create-card">
        <h1>Create a company</h1>
        <p class="subtitle">Set up your organization to manage your team.</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Company Name</label>
            <input
              type="text"
              formControlName="name"
              placeholder="Acme Corp"
              [class.invalid]="isInvalid('name')"
            />
            @if (isInvalid('name')) {
              <span class="field-error">{{ getError('name') }}</span>
            }
          </div>

          @if (serverError()) {
            <p class="error">{{ serverError() }}</p>
          }

          <button class="btn btn-primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Creating...' : 'Create Company' }}
          </button>
        </form>

        <div class="form-footer">
          <a routerLink="/dashboard">Cancel</a>
        </div>
      </div>
    </div>
  `,
})
export class CreateCompanyComponent {
  form: FormGroup;
  loading = signal(false);
  serverError = signal('');

  constructor(
    private fb: FormBuilder,
    private companyService: CompanyService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.errors?.['required']) return 'Company name is required';
    if (control?.errors?.['minlength']) return 'Name must be at least 2 characters';
    if (control?.errors?.['maxlength']) return 'Name must be under 100 characters';
    return '';
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.companyService.createCompany(this.form.get('name')?.value).subscribe({
      next: () => this.router.navigate(['/company']),
      error: (err) => {
        this.serverError.set(err.error?.error || 'Failed to create company');
        this.loading.set(false);
      },
    });
  }
}
