import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { BlogPost } from '../../core/models/blog-post.model';
import { AuthService } from '../../core/services/auth.service';
import { BlogService } from '../../core/services/blog.service';

@Component({
  selector: 'app-blog-list-page',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './blog-list-page.component.html',
  styleUrls: ['./blog-list-page.component.css']
})
export class BlogListPage implements OnInit {
  private readonly blogService = inject(BlogService);
  private readonly auth = inject(AuthService);

  readonly posts = signal<BlogPost[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.blogService
      .listPosts()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (posts) => this.posts.set(posts),
        error: () => this.posts.set([])
      });
  }

  canCreate(): boolean {
    return this.auth.hasAnyRole(['CONTRIBUTOR', 'ADMIN']);
  }

  excerpt(post: BlogPost): string {
    return post.body.length > 180 ? `${post.body.slice(0, 180)}...` : post.body;
  }
}
