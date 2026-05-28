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
  { path: '**', redirectTo: 'login' },
  {
  path: 'verify-email-sent',
  loadComponent: () => import('./auth/verify-email-sent/verify-email-sent.component')
    .then(m => m.VerifyEmailSentComponent)
},
{
  path: 'verify-email',
  loadComponent: () => import('./auth/verify-email/verify-email.component')
    .then(m => m.VerifyEmailComponent)
},
{
  path: 'verify-email-error',
  loadComponent: () => import('./auth/verify-email-error/verify-email-error.component')
    .then(m => m.VerifyEmailErrorComponent)
},
];