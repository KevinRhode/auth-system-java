import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface CompanyMemberDto {
  id: string;
  userId: string;
  name: string;
  email: string;
  role: string;
  joinedAt: string;
}

export interface CompanyDto {
  id: string;
  name: string;
  slug: string;
  createdAt: string;
  members: CompanyMemberDto[];
}

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private api = `${environment.apiUrl}/api/company`;

  constructor(private http: HttpClient) {}

  createCompany(name: string) {
    return this.http.post<CompanyDto>(this.api,
      { name },
      { withCredentials: true }
    );
  }

  getMyCompany() {
    return this.http.get<CompanyDto>(`${this.api}/me`, {
      withCredentials: true
    });
  }

  inviteMember(companyId: string, email: string, role: string) {
    return this.http.post<CompanyMemberDto>(
      `${this.api}/${companyId}/members`,
      { email, role },
      { withCredentials: true }
    );
  }

  updateMemberRole(companyId: string, memberId: string, role: string) {
    return this.http.put<CompanyMemberDto>(
      `${this.api}/${companyId}/members/${memberId}/role`,
      { role },
      { withCredentials: true }
    );
  }

  removeMember(companyId: string, memberId: string) {
    return this.http.delete(
      `${this.api}/${companyId}/members/${memberId}`,
      { withCredentials: true }
    );
  }

  leaveCompany() {
    return this.http.delete(`${this.api}/me/leave`, {
      withCredentials: true
    });
  }
}