import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const reqWithCredentials = req.clone({
    withCredentials: true  // sends cookies automatically on every request
  });
  return next(reqWithCredentials);
};