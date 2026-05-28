import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenService {

  // cookies are httpOnly — JS cannot read them
  // browser sends them automatically on every request

  isLoggedIn(): boolean {
    // since we can't read httpOnly cookies from JS,
    // track login state in memory or sessionStorage
    return sessionStorage.getItem('authenticated') === 'true';
  }

  setAuthenticated() {
    sessionStorage.setItem('authenticated', 'true');
  }

  clearAuthenticated() {
    sessionStorage.removeItem('authenticated');
  }
}