import { BlogPost } from './blog-post.model';
import { Rating } from './rating.model';

export interface UserProfile {
  id: string;
  displayName: string;
  bio: string;
  profilePictureUrl?: string | null;
  ratings: Rating[];
  blogPosts: BlogPost[];
}

export interface UserProfileUpdateRequest {
  displayName: string;
  bio: string;
  profilePictureUrl?: string | null;
}
