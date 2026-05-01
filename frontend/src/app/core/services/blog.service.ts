import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BlogPost, BlogPostCreateRequest } from '../models/blog-post.model';

@Injectable({
  providedIn: 'root'
})
export class BlogService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  listPosts(): Observable<BlogPost[]> {
    return this.http.get<BlogPost[]>(`${this.apiUrl}/blog-posts`);
  }

  getPostBySlug(slug: string): Observable<BlogPost> {
    return this.http.get<BlogPost>(`${this.apiUrl}/blog-posts/${encodeURIComponent(slug)}`);
  }

  createPost(request: BlogPostCreateRequest): Observable<BlogPost> {
    return this.http.post<BlogPost>(`${this.apiUrl}/blog-posts`, request);
  }

  updatePost(id: string, request: BlogPostCreateRequest): Observable<BlogPost> {
    return this.http.put<BlogPost>(`${this.apiUrl}/blog-posts/${encodeURIComponent(id)}`, request);
  }

  deletePost(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/blog-posts/${encodeURIComponent(id)}`);
  }
}
