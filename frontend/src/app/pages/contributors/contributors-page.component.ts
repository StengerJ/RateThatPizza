import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { ContributorProfileSummary } from '../../core/models/profile.model';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-contributors-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './contributors-page.component.html',
  styleUrls: ['./contributors-page.component.css']
})
export class ContributorsPage implements OnInit {
  private readonly profileService = inject(ProfileService);

  readonly contributors = signal<ContributorProfileSummary[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.profileService
      .listContributors()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (contributors) => this.contributors.set(contributors),
        error: () => {
          this.contributors.set([]);
          this.errorMessage.set('Contributors could not be loaded.');
        }
      });
  }

  profileInitials(displayName: string): string {
    return displayName
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('') || 'P';
  }
}
