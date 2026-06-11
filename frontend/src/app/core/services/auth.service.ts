// core/services/auth.service.ts
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

export interface UserDto {
  id: string;
  name: string;
  email: string;
  role: string;
  emailVerified: boolean;
  createdAt: string;
}

interface AuthResponse {
  sessionId: string;
  user: UserDto;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = `${environment.apiUrl}/api/auth`;

  currentUser = signal<UserDto | null>(null);
  currentSessionId = signal<string | null>(null);

  constructor(
    private http: HttpClient,
    public tokenService: TokenService,
    private router: Router,
  ) {}

  register(data: { name: string; email: string; password: string }) {
    return this.http.post<UserDto>(`${this.api}/register`, data, {
      withCredentials: true,
    });
    // no tap() — no setAuthenticated, no currentUser.set
  }

  login(data: { email: string; password: string }) {
    return this.http
      .post<AuthResponse>(`${this.api}/login`, data, {
        withCredentials: true,
      })
      .pipe(
        tap((res) => {
          this.tokenService.setAuthenticated();
          this.currentSessionId.set(res.sessionId);
          this.currentUser.set(res.user);
        }),
      );
  }

  logout() {
    return this.http
      .post(
        `${this.api}/logout`,
        {},
        {
          withCredentials: true,
        },
      )
      .pipe(
        tap(() => {
          this.tokenService.clearAuthenticated();
          this.currentUser.set(null);
          this.currentSessionId.set(null);
          this.router.navigate(['/login']);
        }),
      );
  }

  refresh() {
    return this.http
      .post<AuthResponse>(
        `${this.api}/refresh`,
        {},
        {
          withCredentials: true,
        },
      )
      .pipe(
        tap((res) => {
          this.currentSessionId.set(res.sessionId);
          this.currentUser.set(res.user);
        }),
      );
  }

  forgotPassword(email: string) {
    return this.http.post<{ message: string }>(`${this.api}/forgot-password`,
      { email },
      { withCredentials: true }
    );
  }

  resetPassword(token: string, newPassword: string) {
    return this.http.post<{ message: string }>(`${this.api}/reset-password`,
      { token, newPassword },
      { withCredentials: true }
    );
  }
}
