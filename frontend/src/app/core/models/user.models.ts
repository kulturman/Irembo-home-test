export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

export interface UserResponse {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  tenantId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  name: string;
  role: UserRole;
}

export interface ResourceId {
  id: string;
}
