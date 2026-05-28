import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, Observable, from } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TokenService } from '../services/token.service';
import { environment } from '../../../environments/environment';

let isRefreshing = false;

export const jwtInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {

  const router = inject(Router);
  const tokenService = inject(TokenService);
  const http = inject(HttpClient);

  const reqWithCredentials = req.clone({ withCredentials: true });

  return next(reqWithCredentials).pipe(
    catchError((error: HttpErrorResponse): Observable<HttpEvent<unknown>> => {
      const isExpired = error.status === 401 && error.error?.error === 'TOKEN_EXPIRED';
      const isRefreshCall = req.url.includes('/api/auth/refresh');
      const isAuthCall = req.url.includes('/api/auth/login') ||
                         req.url.includes('/api/auth/register');

      if (!isExpired || isRefreshCall || isAuthCall) {
        if (error.status === 401) {
          tokenService.clearAuthenticated();
          router.navigate(['/login']);
        }
        return throwError(() => error);
      }

      if (isRefreshing) {
        tokenService.clearAuthenticated();
        router.navigate(['/login']);
        return throwError(() => error);
      }

      isRefreshing = true;

      return http.post<void>(
        `${environment.apiUrl}/api/auth/refresh`,
        {},
        { withCredentials: true }
      ).pipe(
        switchMap(() => {
          isRefreshing = false;
          return next(reqWithCredentials);
        }),
        catchError((refreshError) => {
          isRefreshing = false;
          tokenService.clearAuthenticated();
          router.navigate(['/login']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};