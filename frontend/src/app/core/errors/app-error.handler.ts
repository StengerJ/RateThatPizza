import { ErrorHandler, Injectable, inject } from '@angular/core';

import { ClientLoggerService } from '../services/client-logger.service';

@Injectable()
export class AppErrorHandler implements ErrorHandler {
  private readonly logger = inject(ClientLoggerService);

  handleError(error: unknown): void {
    this.logger.logError(error, {
      source: 'global-error-handler'
    });
  }
}
