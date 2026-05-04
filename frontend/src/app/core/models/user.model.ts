export type UserRole = 'PENDING_CONTRIBUTOR' | 'CONTRIBUTOR' | 'ADMIN';

export type UserStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'DISABLED';

export interface User {
  id?: string;
  email: string;
  displayName: string;
  profileBio?: string;
  profilePictureUrl?: string | null;
  role: UserRole;
  status: UserStatus;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthSession {
  token: string;
  user: User;
}

export type AuthResponse = AuthSession;
