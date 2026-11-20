import { User } from "../models/user.model";


export interface LoginRequest {
  email: string;
  password: string;
}


export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
}


export interface RefreshTokenRequest {
  refreshToken: string;
}


export interface LogoutRequest {
  refreshToken: string;
}