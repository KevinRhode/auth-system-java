import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavComponent } from '../../shared/nav/nav.component';
import { FooterComponent } from '../../shared/footer/footer.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, NavComponent, FooterComponent],
  template: `
    <app-nav />
    <main class="main-content">
      <router-outlet />
    </main>
    <app-footer />
  `,
  styles: [`
    :host { display: flex; flex-direction: column; min-height: 100vh; }
    .main-content { flex: 1; padding: 2rem; }
  `]
})
export class AppLayoutComponent {}