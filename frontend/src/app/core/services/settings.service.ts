import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CompanyDto, CompanyService } from './company.service';
import { TokenService } from './token.service';
import { Router } from '@angular/router';

export interface UserDto {
  id: string;
  name: string;
  email: string;
  role: string;
  emailVerified: boolean;
  createdAt: string;
}

export interface UserSettingsDto {
  id: string;
  language: string | null;
  theme: string;
  user: UserDto;
  company: CompanyDto | null;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  theme = signal<string | null>(null);
  language = signal<string | null>(null);
  user = signal<UserDto | null>(null);
  company = signal<CompanyDto | null>(null);

  isDarkTheme = computed(() => this.theme() === 'dark');

  constructor(
    private http: HttpClient,
    private companyService: CompanyService,
  ) {
    // 2. Fetch data once on service initialization
    this.fetchUserSettings();
  }

  // 3. Keep logic clean by isolating side-effects from getters
  private fetchUserSettings(): void {
    this.http
      .get<UserSettingsDto>(`${environment.apiUrl}/api/user/settings`, { withCredentials: true })
      .subscribe({
        next: (settings: UserSettingsDto) => {
          this.theme.set(settings.theme);
          this.language.set(settings.language);
          this.user.set(settings.user);
          this.company.set(settings.company);
        },
        error: (err) => console.error('Failed to load user settings', err),
      });
  }
}
