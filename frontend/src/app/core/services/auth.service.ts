// core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = `${environment.apiUrl}/api/auth`;

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private router: Router
  ) {}

  register(data: { name: string; email: string; password: string }) {
    return this.http.post<any>(`${this.api}/register`, data).pipe(
      tap(res => this.tokenService.setTokens(res.accessToken, res.refreshToken))
    );
  }

  login(data: { email: string; password: string }) {
    return this.http.post<any>(`${this.api}/login`, data).pipe(
      tap(res => this.tokenService.setTokens(res.accessToken, res.refreshToken))
    );
  }

  logout() {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http.post(`${this.api}/logout`, { refreshToken }).pipe(
      tap(() => {
        this.tokenService.clearTokens();
        this.router.navigate(['/login']);
      })
    );
  }

  refresh() {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http.post<any>(`${this.api}/refresh`, { refreshToken }).pipe(
      tap(res => this.tokenService.setTokens(res.accessToken, res.refreshToken))
    );
  }
}