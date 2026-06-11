import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CompanyDto, CompanyService } from './company.service';
import { TokenService } from './token.service';
import { Router } from '@angular/router';

export interface SettingsDto {
  theme: string;
  emailNotifications: boolean;
  companyId: string;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  theme = signal<string | null>(null);
  emailNotifications = false;
  companyId = signal<string | null>(null);

  isDarkTheme = computed(() => this.theme() === 'dark');

  constructor(
    private http: HttpClient,
    private companyService: CompanyService,
  ) {
    // 2. Fetch data once on service initialization
    this.fetchCompanyData();
  }

  // 3. Keep logic clean by isolating side-effects from getters
  private fetchCompanyData(): void {
    this.companyService.getMyCompany().subscribe({
      next: (company: CompanyDto) => this.companyId.set(company.id),
      error: (err) => console.error('Failed to load company ID', err),
    });
  }
}
