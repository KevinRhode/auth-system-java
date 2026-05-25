import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    title: 'Auth System Java',
    loadComponent: () => import('./auth/login/login.component.ts')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    title: 'Auth System Java',
    loadComponent: () => import('./auth/register/register.component.ts')
      .then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    title: 'Dashboard',
    loadComponent: () => import('./dashboard/dashboard.component')
      .then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: 'login' }
];