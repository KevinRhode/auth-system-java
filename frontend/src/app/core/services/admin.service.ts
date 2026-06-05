import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { UserDto } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private api = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  getUsers() {
    return this.http.get<UserDto[]>(`${this.api}/users`, {
      withCredentials: true
    });
  }

  updateRole(id: string, role: string) {
    return this.http.put<UserDto>(`${this.api}/users/${id}/role`,
      { role },
      { withCredentials: true }
    );
  }

  deleteUser(id: string) {
    return this.http.delete(`${this.api}/users/${id}`, {
      withCredentials: true
    });
  }
}