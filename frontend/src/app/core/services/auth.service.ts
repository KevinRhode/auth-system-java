// core/services/auth.service.ts
import { Injectable } from '@angular/core';
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
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = `${environment.apiUrl}/api/auth`;

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private router: Router
  ) {}

  register(data: { name: string; email: string; password: string }) {
    return this.http.post<UserDto>(`${this.api}/register`, data, {
      withCredentials: true
    }).pipe(
      tap(() => this.tokenService.setAuthenticated())
    );
  }

  login(data: { email: string; password: string }) {
    return this.http.post<UserDto>(`${this.api}/login`, data, {
      withCredentials: true
    }).pipe(
      tap(() => this.tokenService.setAuthenticated())
    );
  }

  logout() {
    return this.http.post(`${this.api}/logout`, {}, {
      withCredentials: true
    }).pipe(
      tap(() => {
        this.tokenService.clearAuthenticated();
        this.router.navigate(['/login']);
      })
    );
  }

  refresh() {
    return this.http.post<UserDto>(`${this.api}/refresh`, {}, {
      withCredentials: true
    });
  }
}