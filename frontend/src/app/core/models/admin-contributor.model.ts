import { UserStatus } from './user.model';

export interface AdminContributorRating {
  id: string;
  restaurantName: string;
  location: string;
  overallRating: number;
  affordabilityRating: number;
  createdAt: string;
}

export interface AdminContributorBlogPost {
  id: string;
  title: string;
  slug: string;
  location: string;
  createdAt: string;
}

export interface AdminContributor {
  id: string;
  email: string;
  displayName: string;
  status: UserStatus;
  createdAt: string;
  ratingCount: number;
  blogPostCount: number;
  ratings: AdminContributorRating[];
  blogPosts: AdminContributorBlogPost[];
}
