import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  register(data: { name: string; email: string; password: string }) {
    return this.http.post(`${this.api}/api/auth/register`, data);
  }

  login(data: { email: string; password: string }) {
    return this.http.post(`${this.api}/api/auth/login`, data);
  }

  logout() {
    return this.http.post(`${this.api}/api/auth/logout`, {});
  }
}