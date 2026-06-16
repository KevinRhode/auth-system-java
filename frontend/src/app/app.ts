import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { NavComponent } from './shared/nav/nav.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavComponent],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.scss',
})
export class App implements OnInit {
  constructor(protected authService: AuthService) {}

  ngOnInit() {
    // rehydrate user on every page load
    if (this.authService.tokenService.isLoggedIn()) {
      this.authService.refresh().subscribe({
        error: () => {
          // refresh token expired or invalid — clear and go to login
          this.authService.tokenService.clearAuthenticated();
        },
      });
    }
  }
}
