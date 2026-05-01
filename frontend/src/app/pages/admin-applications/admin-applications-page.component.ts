import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { ApplicationStatus, ContributorApplication } from '../../core/models/application.model';
import { ApplicationsService } from '../../core/services/applications.service';

@Component({
  selector: 'app-admin-applications-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-applications-page.component.html',
  styleUrls: ['./admin-applications-page.component.css']
})
export class AdminApplicationsPage implements OnInit {
  private readonly applicationsService = inject(ApplicationsService);

  readonly applications = signal<ContributorApplication[]>([]);
  readonly processingIds = signal<Set<string>>(new Set());
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.applicationsService
      .listApplications()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (applications) => this.applications.set(applications),
        error: () => this.applications.set([])
      });
  }

  approve(application: ContributorApplication): void {
    this.updateApplication(application.id, 'APPROVED');
  }

  reject(application: ContributorApplication): void {
    this.updateApplication(application.id, 'REJECTED');
  }

  isProcessing(id: string): boolean {
    return this.processingIds().has(id);
  }

  private updateApplication(id: string, status: ApplicationStatus): void {
    this.errorMessage.set('');
    this.setProcessing(id, true);

    const request =
      status === 'APPROVED'
        ? this.applicationsService.approveApplication(id)
        : this.applicationsService.rejectApplication(id);

    request.subscribe({
      next: (updatedApplication) => {
        this.applications.update((applications) =>
          applications.map((application) =>
            application.id === id ? updatedApplication : application
          )
        );
        this.setProcessing(id, false);
      },
      error: () => {
        this.errorMessage.set('Application status could not be updated.');
        this.setProcessing(id, false);
      }
    });
  }

  private setProcessing(id: string, processing: boolean): void {
    const nextIds = new Set(this.processingIds());

    if (processing) {
      nextIds.add(id);
    } else {
      nextIds.delete(id);
    }

    this.processingIds.set(nextIds);
  }
}
