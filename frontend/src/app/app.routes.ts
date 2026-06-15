import { AppLayoutComponent } from './layouts/app-layout/app-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/role.guard';
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'verify-email-sent',
    loadComponent: () =>
      import('./auth/verify-email-sent/verify-email-sent.component').then(
        (m) => m.VerifyEmailSentComponent,
      ),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./auth/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent,
      ),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent,
      ),
  },
  {
    path: 'verify-email',
    loadComponent: () =>
      import('./auth/verify-email/verify-email.component').then((m) => m.VerifyEmailComponent),
  },
  {
    path: 'verify-email-error',
    loadComponent: () =>
      import('./auth/verify-email-error/verify-email-error.component').then(
        (m) => m.VerifyEmailErrorComponent,
      ),
  },
  {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'admin/users',
        loadComponent: () => import('./admin/users/users.component').then((m) => m.UsersComponent),
        canActivate: [adminGuard],
      },
      {
        path: 'company',
        loadComponent: () =>
          import('./admin/company/company.component').then((m) => m.CompanyComponent),
        canActivate: [authGuard],
      },
      {
        path: 'company/create',
        loadComponent: () =>
          import('./admin/company/create-company/create-company.component').then(
            (m) => m.CreateCompanyComponent,
          ),
        canActivate: [authGuard],
      },
      { path: 'settings',
        loadComponent: () =>
          import('./settings/settings.component').then((m) => m.SettingsComponent),
        canActivate: [authGuard],
      },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
