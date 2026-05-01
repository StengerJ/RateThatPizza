export interface Rating {
  id?: string;
  restaurantName: string;
  sauce: string;
  toppings: string;
  crust: string;
  overallRating: number;
  comments: string;
  createdAt?: string;
}

export type RatingCreateRequest = Omit<Rating, 'id' | 'createdAt'>;
