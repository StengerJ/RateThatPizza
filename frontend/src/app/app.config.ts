import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  ErrorHandler,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { AppErrorHandler } from './core/errors/app-error.handler';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorLoggingInterceptor } from './core/interceptors/error-logging.interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideHttpClient(withInterceptors([authInterceptor, errorLoggingInterceptor])),
    provideRouter(routes),
    { provide: ErrorHandler, useClass: AppErrorHandler }
  ]
};
