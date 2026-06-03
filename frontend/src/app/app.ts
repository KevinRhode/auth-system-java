import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  constructor(private authService: AuthService) {}

  ngOnInit() {
    // rehydrate user on every page load
    if (this.authService.tokenService.isLoggedIn()) {
      this.authService.refresh().subscribe({
        error: () => {
          // refresh token expired or invalid — clear and go to login
          this.authService.tokenService.clearAuthenticated();
        }
      });
    }
  }
  
}
