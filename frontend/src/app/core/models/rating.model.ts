export interface Rating {
  id?: string;
  creatorId?: string;
  creator?: string;
  restaurantName: string;
  location: string;
  sauce: string;
  toppings: string;
  crust: string;
  overallRating: number;
  affordabilityRating: number;
  comments: string;
  createdAt?: string;
}

export type RatingCreateRequest = Omit<Rating, 'id' | 'creatorId' | 'creator' | 'createdAt'>;
