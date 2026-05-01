import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { ClientLoggerService } from '../services/client-logger.service';

export const errorLoggingInterceptor: HttpInterceptorFn = (request, next) => {
  const logger = inject(ClientLoggerService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (!request.url.includes('/client-logs')) {
        logger.logError(error, {
          source: 'http-interceptor',
          method: request.method,
          url: request.urlWithParams
        });
      }

      return throwError(() => error);
    })
  );
};
