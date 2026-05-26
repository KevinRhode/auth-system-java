import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { AppLayoutComponent } from './layouts/app-layout/app-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    title: 'Auth System Java',
    loadComponent: () => import('./auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    title: 'Auth System Java',
    loadComponent: () => import('./auth/register/register.component')
      .then(m => m.RegisterComponent)
  },
 {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component')
          .then(m => m.DashboardComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];