export type Role = 'LEARNER' | 'ADMIN';
export type VocabularyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1';

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  currentLevel: VocabularyLevel | null;
  xp: number;
  streak: number;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistrationRequest {
  name: string;
  email: string;
  password: string;
}
