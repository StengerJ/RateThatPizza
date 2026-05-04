import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { BlogPost } from '../../core/models/blog-post.model';
import { UserProfile } from '../../core/models/profile.model';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css']
})
export class ProfilePage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly profileService = inject(ProfileService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly profile = signal<UserProfile | null>(null);
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly isOwnProfile = computed(() => {
    const profile = this.profile();
    const user = this.auth.currentUser();
    return Boolean(profile?.id && user?.id === profile.id);
  });

  readonly form = this.fb.group({
    displayName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    bio: ['', [Validators.maxLength(500)]],
    profilePictureUrl: ['', [Validators.maxLength(500)]]
  });

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const profileId = params.get('id');

      if (!profileId) {
        this.loading.set(false);
        this.errorMessage.set('Profile could not be loaded.');
        return;
      }

      this.loadProfile(profileId);
    });
  }

  startEditing(profile: UserProfile): void {
    this.patchForm(profile);
    this.editing.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  cancelEditing(): void {
    const profile = this.profile();
    if (profile) {
      this.patchForm(profile);
    }

    this.editing.set(false);
    this.errorMessage.set('');
  }

  saveProfile(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving.set(true);

    this.profileService.updateMyProfile({
      displayName: value.displayName.trim(),
      bio: value.bio.trim(),
      profilePictureUrl: value.profilePictureUrl.trim()
    }).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.patchForm(profile);
        this.editing.set(false);
        this.saving.set(false);
        this.successMessage.set('Profile updated.');
        this.auth.refreshCurrentUser().subscribe({ error: () => undefined });
      },
      error: () => {
        this.errorMessage.set('Profile could not be saved.');
        this.saving.set(false);
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

  excerpt(post: BlogPost): string {
    return post.body.length > 140 ? `${post.body.slice(0, 140)}...` : post.body;
  }

  private loadProfile(profileId: string): void {
    this.loading.set(true);
    this.profile.set(null);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.editing.set(false);

    this.profileService.getProfile(profileId).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.patchForm(profile);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Profile could not be loaded.');
        this.loading.set(false);
      }
    });
  }

  private patchForm(profile: UserProfile): void {
    this.form.patchValue({
      displayName: profile.displayName,
      bio: profile.bio ?? '',
      profilePictureUrl: profile.profilePictureUrl ?? ''
    });
  }
}
