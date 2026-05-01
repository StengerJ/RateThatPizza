export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ContributorApplication {
  id: string;
  email: string;
  displayName: string;
  applicationReason: string;
  status: ApplicationStatus;
  createdAt: string;
}

export interface ContributorApplicationRequest {
  email: string;
  displayName: string;
  password: string;
  applicationReason: string;
}
