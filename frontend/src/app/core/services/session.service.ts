import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface SessionDto {
  id: string;
  userAgent: string;
  createdAt: string;
  expiresAt: string;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private api = `${environment.apiUrl}/api/sessions`;

  constructor(private http: HttpClient) {}

  getSessions() {
    return this.http.get<SessionDto[]>(this.api);
  }

  revokeSession(id: string) {
    return this.http.delete(`${this.api}/${id}`);
  }

  revokeAllSessions() {
    return this.http.delete(`${this.api}/all`);
  }
}